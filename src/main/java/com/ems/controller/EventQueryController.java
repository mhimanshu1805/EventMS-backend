package com.ems.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ems.dto.QueryRequest;
import com.ems.dto.QueryResponse;
import com.ems.service.QueryService;

import jakarta.validation.Valid;

/**
 * Separate from EventController so the class-level ORGANIZER check does not apply.
 */
@RestController
@RequestMapping("/api/events")
public class EventQueryController {

    private final QueryService queryService;

    public EventQueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/{eventId}/queries")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<QueryResponse> create(
            @PathVariable Long eventId,
            @Valid @RequestBody QueryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(queryService.create(eventId, request));
    }
}
