package com.ems.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ems.dto.EventRequest;
import com.ems.dto.EventResponse;
import com.ems.dto.EventStatsResponse;
import com.ems.dto.EventStatusRequest;
import com.ems.dto.QueryResponse;
import com.ems.dto.RegistrationResponse;
import com.ems.service.EventService;
import com.ems.service.QrCodeService;
import com.ems.service.QueryService;
import com.ems.service.RegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
@PreAuthorize("hasRole('ORGANIZER')")
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final QueryService queryService;
    private final QrCodeService qrCodeService;

    public EventController(
            EventService eventService,
            RegistrationService registrationService,
            QueryService queryService,
            QrCodeService qrCodeService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.queryService = queryService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request));
    }

    @GetMapping("/my")
    public List<EventResponse> listMine() {
        return eventService.listMine();
    }

    @GetMapping("/{id}")
    public EventResponse getById(@PathVariable Long id) {
        return eventService.getOwnedById(id);
    }

    @PutMapping("/{id}")
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return eventService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public EventResponse updateStatus(@PathVariable Long id, @Valid @RequestBody EventStatusRequest request) {
        return eventService.updateStatus(id, request.getStatus());
    }

    @PostMapping(value = "/{id}/poster", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EventResponse uploadPoster(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return eventService.uploadPoster(id, file);
    }

    @DeleteMapping("/{id}/poster")
    public EventResponse deletePoster(@PathVariable Long id) {
        return eventService.deletePoster(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}/registrations")
    public List<RegistrationResponse> listRegistrations(@PathVariable Long eventId) {
        return registrationService.listForOwnedEvent(eventId);
    }

    @GetMapping("/{eventId}/stats")
    public EventStatsResponse stats(@PathVariable Long eventId) {
        return registrationService.statsForOwnedEvent(eventId);
    }

    @GetMapping("/{eventId}/queries")
    public List<QueryResponse> listQueries(@PathVariable Long eventId) {
        return queryService.listForOwnedEvent(eventId);
    }

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCodeService.pngForOwnedEvent(id));
    }
}
