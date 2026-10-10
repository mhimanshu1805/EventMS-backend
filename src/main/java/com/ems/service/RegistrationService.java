package com.ems.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dto.EventStatsResponse;
import com.ems.dto.RegisterForEventRequest;
import com.ems.dto.RegistrationResponse;
import com.ems.entity.Event;
import com.ems.entity.EventStatus;
import com.ems.entity.Registration;
import com.ems.entity.RegistrationStatus;
import com.ems.entity.User;
import com.ems.exception.BadRequestException;
import com.ems.exception.DuplicateResourceException;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.EventRepository;
import com.ems.repository.RegistrationRepository;
import com.ems.repository.UserRepository;

@Service
@Transactional
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    public RegistrationService(
            RegistrationRepository registrationRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            EventService eventService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public RegistrationResponse register(Long eventId, RegisterForEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("Registration is only open for PUBLISHED events");
        }

        User participant = currentUser();
        var existing = registrationRepository.findByEventAndParticipant(event, participant);
        if (existing.isPresent()) {
            Registration registration = existing.get();
            if (registration.getStatus() != RegistrationStatus.CANCELLED) {
                throw new DuplicateResourceException("You are already registered for this event");
            }
            // Unique (event, participant): reopen a cancelled row instead of inserting another.
            ensureCapacityAvailable(event);
            registration.setPhone(request.getPhone().trim());
            registration.setStatus(RegistrationStatus.PENDING);
            return toResponse(registrationRepository.save(registration));
        }

        ensureCapacityAvailable(event);
        Registration saved = registrationRepository.save(Registration.builder()
                .event(event)
                .participant(participant)
                .phone(request.getPhone().trim())
                .status(RegistrationStatus.PENDING)
                .build());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> listMine() {
        return registrationRepository.findByParticipantOrderByRegisteredAtDesc(currentUser()).stream()
                .map(this::toResponse)
                .toList();
    }

    public RegistrationResponse cancelOwn(Long id) {
        Registration registration = getRegistration(id);
        if (!registration.getParticipant().getId().equals(currentUser().getId())) {
            throw new AccessDeniedException("You can only cancel your own registration");
        }
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new BadRequestException("Registration is already cancelled");
        }
        registration.setStatus(RegistrationStatus.CANCELLED);
        return toResponse(registrationRepository.save(registration));
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> listForOwnedEvent(Long eventId) {
        Event event = eventService.requireOwnedEvent(eventId);
        return registrationRepository.findByEventOrderByRegisteredAtDesc(event).stream()
                .map(this::toResponse)
                .toList();
    }

    public RegistrationResponse updateStatus(Long id, RegistrationStatus status) {
        Registration registration = getRegistration(id);
        eventService.requireOwnedEvent(registration.getEvent().getId());
        boolean becomingActive = status != RegistrationStatus.CANCELLED
                && registration.getStatus() == RegistrationStatus.CANCELLED;
        if (becomingActive) {
            ensureCapacityAvailable(registration.getEvent());
        }
        registration.setStatus(status);
        return toResponse(registrationRepository.save(registration));
    }

    @Transactional(readOnly = true)
    public EventStatsResponse statsForOwnedEvent(Long eventId) {
        Event event = eventService.requireOwnedEvent(eventId);
        long active = registrationRepository.countByEventAndStatusNot(event, RegistrationStatus.CANCELLED);
        long confirmed = registrationRepository.countByEventAndStatus(event, RegistrationStatus.CONFIRMED);
        long cancelled = registrationRepository.countByEventAndStatus(event, RegistrationStatus.CANCELLED);
        return EventStatsResponse.builder()
                .eventId(event.getId())
                .capacity(event.getCapacity())
                .totalRegistrations(active)
                .confirmed(confirmed)
                .cancelled(cancelled)
                .remainingCapacity(event.getCapacity() - active)
                .build();
    }

    /** PENDING and CONFIRMED take a seat; CANCELLED does not. */
    private void ensureCapacityAvailable(Event event) {
        long active = registrationRepository.countByEventAndStatusNot(event, RegistrationStatus.CANCELLED);
        if (active >= event.getCapacity()) {
            throw new BadRequestException("Event is at full capacity");
        }
    }

    private Registration getRegistration(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private RegistrationResponse toResponse(Registration registration) {
        Event event = registration.getEvent();
        User participant = registration.getParticipant();
        return RegistrationResponse.builder()
                .id(registration.getId())
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .eventSlug(event.getSlug())
                .participantId(participant.getId())
                .participantName(participant.getName())
                .participantEmail(participant.getEmail())
                .phone(registration.getPhone())
                .status(registration.getStatus())
                .registeredAt(registration.getRegisteredAt())
                .build();
    }
}
