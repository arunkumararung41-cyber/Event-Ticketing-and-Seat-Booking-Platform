package com.eventbook.controller;

import com.eventbook.dto.BookingResponse;
import com.eventbook.dto.HoldRequest;
import com.eventbook.security.UserPrincipal;
import com.eventbook.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/hold")
    public ResponseEntity<BookingResponse> holdSeats(@Valid @RequestBody HoldRequest request,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        BookingResponse response = bookingService.holdSeats(principal.getId(), request.getEventId(), request.getSeatIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirm(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookingService.confirmBooking(principal.getId(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        bookingService.cancelBooking(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingResponse>> myBookings(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookingService.myBookings(principal.getId()));
    }
}
