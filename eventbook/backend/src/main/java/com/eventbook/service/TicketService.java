package com.eventbook.service;

import com.eventbook.dto.TicketResponse;
import com.eventbook.entity.BookingSeat;
import com.eventbook.entity.Ticket;
import com.eventbook.exception.ResourceNotFoundException;
import com.eventbook.repository.TicketRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    public Ticket issueTicket(BookingSeat bookingSeat) {
        String code = "TKT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        Ticket ticket = Ticket.builder()
                .bookingSeat(bookingSeat)
                .ticketCode(code)
                .build();
        ticket = ticketRepository.save(ticket);
        log.info("Issued ticket {} for seat {}", code, bookingSeat.getSeat().getSeatNumber());
        return ticket;
    }

    public TicketResponse getTicketWithQr(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        return build(ticket);
    }

    private TicketResponse build(Ticket ticket) {
        String qrBase64 = generateQrBase64(ticket.getTicketCode());
        return TicketResponse.builder()
                .ticketId(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .seatNumber(ticket.getBookingSeat().getSeat().getSeatNumber())
                .eventName(ticket.getBookingSeat().getSeat().getEvent().getName())
                .qrCodeBase64(qrBase64)
                .build();
    }

    private String generateQrBase64(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 250, 250);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for {}", content, e);
            return "";
        }
    }
}
