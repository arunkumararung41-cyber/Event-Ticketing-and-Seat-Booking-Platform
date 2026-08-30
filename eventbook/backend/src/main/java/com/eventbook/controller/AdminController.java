package com.eventbook.controller;

import com.eventbook.dto.SalesSummaryResponse;
import com.eventbook.dto.VenueRequest;
import com.eventbook.entity.Venue;
import com.eventbook.repository.VenueRepository;
import com.eventbook.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final VenueRepository venueRepository;

    @GetMapping("/events/{eventId}/sales-summary")
    public ResponseEntity<SalesSummaryResponse> salesSummary(@PathVariable Long eventId) {
        return ResponseEntity.ok(adminService.getSalesSummary(eventId));
    }

    @PostMapping("/venues")
    public ResponseEntity<Venue> createVenue(@RequestBody Venue venue) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueRepository.save(venue));
    }

    @GetMapping("/venues")
    public ResponseEntity<Iterable<Venue>> listVenues() {
        return ResponseEntity.ok(venueRepository.findAll());
    }
}
