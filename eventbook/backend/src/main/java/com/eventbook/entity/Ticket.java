package com.eventbook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_seat_id", nullable = false, unique = true)
    private BookingSeat bookingSeat;

    @Column(nullable = false, unique = true, length = 64)
    private String ticketCode; // encoded into the QR

    @Column(nullable = false)
    private Instant issuedAt;

    @PrePersist
    public void prePersist() {
        this.issuedAt = Instant.now();
    }
}
