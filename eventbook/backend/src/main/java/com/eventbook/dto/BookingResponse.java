package com.eventbook.dto;

import com.eventbook.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private Long eventId;
    private String eventName;
    private BookingStatus status;
    private List<String> seatNumbers;
    private Instant expiresAt;
    private Instant confirmedAt;
}
