package com.ems.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ems.dto.RegistrationResponse;
import com.ems.dto.RegistrationStatusRequest;
import com.ems.service.RegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public List<RegistrationResponse> listMine() {
        return registrationService.listMine();
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public RegistrationResponse cancel(@PathVariable Long id) {
        return registrationService.cancelOwn(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ORGANIZER')")
    public RegistrationResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody RegistrationStatusRequest request) {
        return registrationService.updateStatus(id, request.getStatus());
    }
}
