package com.eventbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_seats", uniqueConstraints = @UniqueConstraint(columnNames = "seat_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @OneToOne(mappedBy = "bookingSeat", cascade = CascadeType.ALL, orphanRemoval = true)
    private Ticket ticket;
}
