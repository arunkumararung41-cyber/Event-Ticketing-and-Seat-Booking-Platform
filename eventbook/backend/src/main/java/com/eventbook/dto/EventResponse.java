package com.eventbook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String venueName;
    private String city;
    private Instant eventDate;
    private BigDecimal basePrice;
    private String status;
    private long availableSeats;
    private long totalSeats;
}
