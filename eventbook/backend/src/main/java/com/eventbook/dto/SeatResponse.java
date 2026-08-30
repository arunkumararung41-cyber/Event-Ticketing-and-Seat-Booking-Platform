package com.eventbook.dto;

import com.eventbook.entity.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class SeatResponse {
    private Long id;
    private String seatNumber;
    private String section;
    private String seatRow;
    private BigDecimal price;
    private SeatStatus status;
}
