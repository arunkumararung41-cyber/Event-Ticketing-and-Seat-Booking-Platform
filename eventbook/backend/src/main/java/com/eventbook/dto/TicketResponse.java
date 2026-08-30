package com.eventbook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TicketResponse {
    private Long ticketId;
    private String ticketCode;
    private String seatNumber;
    private String eventName;
    private String qrCodeBase64;
}
