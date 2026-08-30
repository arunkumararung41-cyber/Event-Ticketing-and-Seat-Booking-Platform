package com.eventbook.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class EventRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String category;

    @NotNull
    private Long venueId;

    @NotNull
    @Future(message = "Event date must be in the future")
    private Instant eventDate;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal basePrice;

    // Simple rectangular seat map generator: rows x seatsPerRow
    @Min(1)
    private int rows = 10;

    @Min(1)
    private int seatsPerRow = 10;
}
