package com.eventbook.service;

import com.eventbook.dto.EventRequest;
import com.eventbook.dto.EventResponse;
import com.eventbook.entity.*;
import com.eventbook.exception.ResourceNotFoundException;
import com.eventbook.repository.EventRepository;
import com.eventbook.repository.SeatRepository;
import com.eventbook.repository.UserRepository;
import com.eventbook.repository.VenueRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @Transactional
    public EventResponse createEvent(EventRequest request, Long organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        Event event = Event.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .organizer(organizer)
                .venue(venue)
                .eventDate(request.getEventDate())
                .basePrice(request.getBasePrice())
                .status("PUBLISHED")
                .build();
        event = eventRepository.save(event);

        // Generate a simple rectangular seat map: rows labelled A, B, C...; seats numbered within row
        List<Seat> seats = new ArrayList<>();
        for (int r = 0; r < request.getRows(); r++) {
            String rowLabel = String.valueOf((char) ('A' + r));
            // front rows priced higher as a simple realistic pricing rule
            BigDecimal rowMultiplier = BigDecimal.valueOf(1.0 + Math.max(0, (request.getRows() - r - 1)) * 0.05);
            for (int s = 1; s <= request.getSeatsPerRow(); s++) {
                seats.add(Seat.builder()
                        .event(event)
                        .seatRow(rowLabel)
                        .seatNumber(rowLabel + s)
                        .section(r < request.getRows() / 3 ? "Platinum" : r < 2 * request.getRows() / 3 ? "Gold" : "Silver")
                        .price(request.getBasePrice().multiply(rowMultiplier))
                        .status(SeatStatus.AVAILABLE)
                        .build());
            }
        }
        seatRepository.saveAll(seats);
        log.info("Created event id={} with {} seats", event.getId(), seats.size());

        return toResponse(event, seats.size(), seats.size());
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> search(String category, String city, String query, Pageable pageable) {
        Specification<Event> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "PUBLISHED"));
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }
            if (city != null && !city.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.join("venue").get("city")), city.toLowerCase()));
            }
            if (query != null && !query.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return eventRepository.findAll(spec, pageable).map(event -> {
            List<Seat> seats = seatRepository.findByEventId(event.getId());
            long available = seats.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count();
            return toResponse(event, available, seats.size());
        });
    }

    @Transactional(readOnly = true)
    public EventResponse getById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
        List<Seat> seats = seatRepository.findByEventId(id);
        long available = seats.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count();
        return toResponse(event, available, seats.size());
    }

    private EventResponse toResponse(Event event, long available, long total) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .category(event.getCategory())
                .venueName(event.getVenue().getName())
                .city(event.getVenue().getCity())
                .eventDate(event.getEventDate())
                .basePrice(event.getBasePrice())
                .status(event.getStatus())
                .availableSeats(available)
                .totalSeats(total)
                .build();
    }
}
