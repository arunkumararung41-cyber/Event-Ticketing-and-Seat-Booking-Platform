package com.eventbook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class SalesSummaryResponse {
    private Long eventId;
    private String eventName;
    private long totalSeats;
    private long bookedSeats;
    private long heldSeats;
    private long availableSeats;
    private BigDecimal grossRevenue;
}
