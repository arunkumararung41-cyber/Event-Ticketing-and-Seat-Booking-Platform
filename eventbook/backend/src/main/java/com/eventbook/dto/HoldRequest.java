package com.eventbook.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class HoldRequest {

    @NotNull
    private Long eventId;

    @NotEmpty(message = "Select at least one seat")
    private List<Long> seatIds;
}
