package com.eventbook.scheduler;

import com.eventbook.entity.Booking;
import com.eventbook.entity.BookingStatus;
import com.eventbook.repository.BookingRepository;
import com.eventbook.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Redis TTL already makes the *lock* disappear automatically when a hold expires.
 * This job's only responsibility is to reconcile the *database* side — flipping
 * stale HELD bookings/seats to EXPIRED/AVAILABLE — so reporting, seat maps, and
 * "my bookings" screens stay consistent even if a user simply abandoned checkout.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredHoldCleanupJob {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    @Scheduled(fixedDelayString = "${booking.cleanup-interval-ms:60000}")
    public void releaseExpiredHolds() {
        List<Booking> expired = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.HELD, Instant.now());
        if (expired.isEmpty()) return;

        log.info("Cleanup job found {} expired holds to release", expired.size());
        for (Booking booking : expired) {
            bookingService.expireBooking(booking);
        }
    }
}
