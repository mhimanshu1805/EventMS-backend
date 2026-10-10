package com.ems.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ems.dto.RegisterForEventRequest;
import com.ems.dto.RegistrationResponse;
import com.ems.service.RegistrationService;

import jakarta.validation.Valid;

/**
 * Separate from EventController so @PreAuthorize ORGANIZER is not applied here.
 */
@RestController
@RequestMapping("/api/events")
public class EventRegisterController {

    private final RegistrationService registrationService;

    public EventRegisterController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/{eventId}/register")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public ResponseEntity<RegistrationResponse> register(
            @PathVariable Long eventId,
            @Valid @RequestBody RegisterForEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.register(eventId, request));
    }
}
