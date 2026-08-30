package com.eventbook.controller;

import com.eventbook.dto.EventRequest;
import com.eventbook.dto.EventResponse;
import com.eventbook.security.UserPrincipal;
import com.eventbook.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventResponse>> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String query,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.search(category, city, query, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole(\"ADMIN\",\"ORGANIZER\")")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request, principal.getId()));
    }
}
