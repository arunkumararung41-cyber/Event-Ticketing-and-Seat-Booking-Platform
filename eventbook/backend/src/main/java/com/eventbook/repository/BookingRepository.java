package com.eventbook.repository;

import com.eventbook.entity.Booking;
import com.eventbook.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, Instant instant);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
}
