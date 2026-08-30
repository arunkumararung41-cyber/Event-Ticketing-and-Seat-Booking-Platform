package com.eventbook.service;

import com.eventbook.dto.BookingResponse;
import com.eventbook.entity.*;
import com.eventbook.exception.BookingExpiredException;
import com.eventbook.exception.ResourceNotFoundException;
import com.eventbook.exception.SeatUnavailableException;
import com.eventbook.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Implements the two-phase "hold, then confirm" booking flow.
 *
 * Phase 1 (hold): attempt to atomically lock every requested seat in Redis.
 * This is the concurrency-critical section — under load, many threads may
 * race to hold the same seat, and Redis SETNX guarantees only one wins.
 *
 * Phase 2 (confirm): the user completes "payment" (simulated) and we persist
 * the booking as CONFIRMED, mark seats BOOKED in the DB, generate tickets,
 * and release the Redis lock (it has served its purpose).
 *
 * If the hold expires (TTL elapses) before confirm is called, Redis silently
 * drops the lock; a scheduled job (ExpiredHoldCleanupJob) reconciles the DB
 * side by flipping any stale HELD bookings/seats back to AVAILABLE/EXPIRED.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SeatLockService seatLockService;
    private final TicketService ticketService;

    @Value("${booking.hold-duration-minutes:5}")
    private long holdDurationMinutes;

    @Transactional
    public BookingResponse holdSeats(Long userId, Long eventId, List<Long> seatIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new ResourceNotFoundException("One or more selected seats do not exist");
        }
        for (Seat seat : seats) {
            if (!seat.getEvent().getId().equals(eventId)) {
                throw new SeatUnavailableException("Seat " + seat.getSeatNumber() + " does not belong to this event");
            }
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatUnavailableException("Seat " + seat.getSeatNumber() + " is no longer available");
            }
        }

        // Create the booking row first so we have a stable reference id for the Redis lock value
        Instant now = Instant.now();
        Booking booking = Booking.builder()
                .user(user)
                .event(event)
                .status(BookingStatus.HELD)
                .createdAt(now)
                .expiresAt(now.plus(holdDurationMinutes, ChronoUnit.MINUTES))
                .build();
        booking = bookingRepository.save(booking);
        String bookingRef = "booking:" + booking.getId();

        // *** THE CRITICAL CONCURRENCY-SAFE STEP ***
        List<Long> failedSeatIds = seatLockService.tryLockSeats(seatIds, bookingRef);
        if (!failedSeatIds.isEmpty()) {
            bookingRepository.delete(booking);
            throw new SeatUnavailableException(
                    "Seat(s) already being booked by someone else: " + failedSeatIds +
                    ". Please choose different seats.");
        }

        // Locks acquired successfully -> reflect HELD status in the DB too (for admin visibility / reporting)
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.HELD);
        }
        seatRepository.saveAll(seats);

        for (Seat seat : seats) {
            BookingSeat bs = BookingSeat.builder().booking(booking).seat(seat).build();
            booking.getBookingSeats().add(bs);
        }
        bookingSeatRepository.saveAll(booking.getBookingSeats());

        log.info("Booking {} HELD for user {} — seats {} (expires in {} min)",
                booking.getId(), userId, seatIds, holdDurationMinutes);

        return toResponse(booking);
    }

    @Transactional
    public BookingResponse confirmBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new SeatUnavailableException("This booking does not belong to you");
        }
        if (booking.getStatus() != BookingStatus.HELD) {
            throw new BookingExpiredException("Booking is not in a confirmable state: " + booking.getStatus());
        }
        if (Instant.now().isAfter(booking.getExpiresAt())) {
            expireBooking(booking);
            throw new BookingExpiredException("Your seat hold has expired. Please select seats again.");
        }

        String bookingRef = "booking:" + booking.getId();

        // Verify every seat's Redis lock is STILL owned by this booking before finalizing —
        // guards against the edge case where TTL expired a split second before confirm arrived.
        for (BookingSeat bs : booking.getBookingSeats()) {
            if (!seatLockService.isOwnedBy(bs.getSeat().getId(), bookingRef)) {
                expireBooking(booking);
                throw new BookingExpiredException("Hold expired on seat " + bs.getSeat().getSeatNumber() + ". Please try again.");
            }
        }

        // Simulate a payment step succeeding (real integration would call a payment gateway here)
        for (BookingSeat bs : booking.getBookingSeats()) {
            Seat seat = bs.getSeat();
            seat.setStatus(SeatStatus.BOOKED);
            seatRepository.save(seat);
            seatLockService.releaseIfOwnedBy(seat.getId(), bookingRef); // lock has done its job
            ticketService.issueTicket(bs);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(Instant.now());
        bookingRepository.save(booking);

        log.info("Booking {} CONFIRMED for user {}", booking.getId(), userId);
        return toResponse(booking);
    }

    @Transactional
    public void cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getUser().getId().equals(userId)) {
            throw new SeatUnavailableException("This booking does not belong to you");
        }
        if (booking.getStatus() == BookingStatus.HELD) {
            releaseSeatsAndLocks(booking);
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking {} cancelled by user {}", bookingId, userId);
    }

    /** Called by the scheduled cleanup job for holds whose TTL has lapsed. */
    @Transactional
    public void expireBooking(Booking booking) {
        if (booking.getStatus() != BookingStatus.HELD) return;
        releaseSeatsAndLocks(booking);
        booking.setStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);
        log.info("Booking {} EXPIRED — seats released", booking.getId());
    }

    private void releaseSeatsAndLocks(Booking booking) {
        String bookingRef = "booking:" + booking.getId();
        for (BookingSeat bs : booking.getBookingSeats()) {
            seatLockService.releaseIfOwnedBy(bs.getSeat().getId(), bookingRef);
            Seat seat = bs.getSeat();
            if (seat.getStatus() == SeatStatus.HELD) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> myBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .eventId(booking.getEvent().getId())
                .eventName(booking.getEvent().getName())
                .status(booking.getStatus())
                .seatNumbers(booking.getBookingSeats().stream().map(bs -> bs.getSeat().getSeatNumber()).toList())
                .expiresAt(booking.getExpiresAt())
                .confirmedAt(booking.getConfirmedAt())
                .build();
    }
}
