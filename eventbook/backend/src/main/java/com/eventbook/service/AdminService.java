package com.eventbook.service;

import com.eventbook.dto.SalesSummaryResponse;
import com.eventbook.entity.Event;
import com.eventbook.entity.Seat;
import com.eventbook.entity.SeatStatus;
import com.eventbook.exception.ResourceNotFoundException;
import com.eventbook.repository.EventRepository;
import com.eventbook.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public SalesSummaryResponse getSalesSummary(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        List<Seat> seats = seatRepository.findByEventId(eventId);

        long booked = seats.stream().filter(s -> s.getStatus() == SeatStatus.BOOKED).count();
        long held = seats.stream().filter(s -> s.getStatus() == SeatStatus.HELD).count();
        long available = seats.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count();
        BigDecimal revenue = seats.stream()
                .filter(s -> s.getStatus() == SeatStatus.BOOKED)
                .map(Seat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SalesSummaryResponse.builder()
                .eventId(event.getId())
                .eventName(event.getName())
                .totalSeats(seats.size())
                .bookedSeats(booked)
                .heldSeats(held)
                .availableSeats(available)
                .grossRevenue(revenue)
                .build();
    }
}
