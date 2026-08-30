package com.eventbook.controller;

import com.eventbook.dto.SeatResponse;
import com.eventbook.entity.Seat;
import com.eventbook.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatRepository seatRepository;

    @GetMapping
    public ResponseEntity<List<SeatResponse>> getSeatsForEvent(@PathVariable Long eventId) {
        List<Seat> seats = seatRepository.findByEventId(eventId);
        List<SeatResponse> response = seats.stream().map(s -> SeatResponse.builder()
                .id(s.getId())
                .seatNumber(s.getSeatNumber())
                .section(s.getSection())
                .seatRow(s.getSeatRow())
                .price(s.getPrice())
                .status(s.getStatus())
                .build()).toList();
        return ResponseEntity.ok(response);
    }
}
