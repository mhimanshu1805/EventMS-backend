package com.ems.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dto.EventRequest;
import com.ems.dto.EventResponse;
import com.ems.entity.Event;
import com.ems.entity.EventStatus;
import com.ems.entity.User;
import com.ems.exception.BadRequestException;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.EventRepository;
import com.ems.repository.QueryRepository;
import com.ems.repository.RegistrationRepository;
import com.ems.repository.UserRepository;

@Service
@Transactional
public class EventService {

    private static final Path POSTER_DIR = Path.of("uploads", "posters");

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final QueryRepository queryRepository;
    private final PublicEventLinks publicEventLinks;

    public EventService(
            EventRepository eventRepository,
            UserRepository userRepository,
            RegistrationRepository registrationRepository,
            QueryRepository queryRepository,
            PublicEventLinks publicEventLinks) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
        this.queryRepository = queryRepository;
        this.publicEventLinks = publicEventLinks;
    }

    public EventResponse create(EventRequest request) {
        validateDatesAndCapacity(request);
        User organizer = currentUser();
        Event event = Event.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .venue(request.getVenue())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .capacity(request.getCapacity())
                .slug(generateUniqueSlug(request.getTitle()))
                .status(EventStatus.DRAFT)
                .organizer(organizer)
                .build();
        return toResponse(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> listMine() {
        return eventRepository.findByOrganizerOrderByCreatedAtDesc(currentUser()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getOwnedById(Long id) {
        return toResponse(requireOwnedEvent(id));
    }

    public EventResponse update(Long id, EventRequest request) {
        validateDatesAndCapacity(request);
        Event event = requireOwnedEvent(id);
        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription());
        event.setVenue(request.getVenue());
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setCapacity(request.getCapacity());
        // Slug stays the same so public URLs do not break after an edit.
        return toResponse(eventRepository.save(event));
    }

    public EventResponse updateStatus(Long id, EventStatus status) {
        Event event = requireOwnedEvent(id);
        event.setStatus(status);
        return toResponse(eventRepository.save(event));
    }

    public void delete(Long id) {
        deleteWithDependents(requireOwnedEvent(id));
    }

    /**
     * Saves a JPG or PNG for this organizer's event and stores the public path
     * on posterUrl. A previous poster file is removed first.
     */
    public EventResponse uploadPoster(Long id, MultipartFile file) {
        Event event = requireOwnedEvent(id);
        String extension = posterExtension(file);
        deletePosterFile(event.getPosterUrl());
        String suffix = String.format("%04x", ThreadLocalRandom.current().nextInt(0x10000));
        String filename = event.getId() + "-" + suffix + "." + extension;
        try {
            Files.createDirectories(POSTER_DIR);
            file.transferTo(POSTER_DIR.resolve(filename).toAbsolutePath());
        } catch (IOException ex) {
            throw new BadRequestException("Could not save poster");
        }
        event.setPosterUrl("/uploads/posters/" + filename);
        return toResponse(eventRepository.save(event));
    }

    /** Removes the poster file, if any, and clears posterUrl. */
    public EventResponse deletePoster(Long id) {
        Event event = requireOwnedEvent(id);
        deletePosterFile(event.getPosterUrl());
        event.setPosterUrl(null);
        return toResponse(eventRepository.save(event));
    }

    /** Admin removal: any event, not only the caller's. */
    public void deleteAny(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        deleteWithDependents(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> listAll() {
        return eventRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Registrations and queries reference the event, so they must be removed first
     * or the database rejects the delete.
     */
    private void deleteWithDependents(Event event) {
        queryRepository.deleteByEvent(event);
        registrationRepository.deleteByEvent(event);
        deletePosterFile(event.getPosterUrl());
        eventRepository.delete(event);
    }

    /** jpg/jpeg/png only, and the content type must match. Returns the extension to store. */
    private String posterExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Poster must be a JPG or PNG image");
        }
        String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = original.lastIndexOf('.');
        String extension = dot >= 0 ? original.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        boolean jpeg = (type.equals("image/jpeg") || type.equals("image/jpg"))
                && (extension.equals("jpg") || extension.equals("jpeg"));
        boolean png = type.equals("image/png") && extension.equals("png");
        if (png) {
            return "png";
        }
        if (jpeg) {
            return extension.equals("jpeg") ? "jpeg" : "jpg";
        }
        throw new BadRequestException("Poster must be a JPG or PNG image");
    }

    /**
     * Deletes only a file whose name is the last segment of a poster path we stored.
     * Ignores anything with a slash or ".." so a bad value cannot leave this folder.
     */
    private void deletePosterFile(String posterUrl) {
        if (posterUrl == null || posterUrl.isBlank()) {
            return;
        }
        int slash = posterUrl.lastIndexOf('/');
        String name = slash >= 0 ? posterUrl.substring(slash + 1) : posterUrl;
        if (name.isBlank() || name.contains("..") || name.indexOf('\\') >= 0) {
            return;
        }
        try {
            Files.deleteIfExists(POSTER_DIR.resolve(name));
        } catch (IOException ignored) {
            // The database update still proceeds if the file is already gone.
        }
    }

    @Transactional(readOnly = true)
    public List<EventResponse> listPublished(String search) {
        List<Event> events;
        if (search == null || search.isBlank()) {
            events = eventRepository.findByStatus(EventStatus.PUBLISHED);
        } else {
            events = eventRepository.findByStatusAndTitleContainingIgnoreCase(
                    EventStatus.PUBLISHED, search.trim());
        }
        return events.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getPublishedBySlug(String slug) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Event not found");
        }
        return toResponse(event);
    }

    /**
     * 404 if missing, 403 if it belongs to another organizer.
     */
    public Event requireOwnedEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        User organizer = currentUser();
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new AccessDeniedException("You cannot access another organizer's event");
        }
        return event;
    }

    private void validateDatesAndCapacity(EventRequest request) {
        if (!request.getEndDateTime().isAfter(request.getStartDateTime())) {
            throw new BadRequestException("endDateTime must be after startDateTime");
        }
        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new BadRequestException("capacity must be greater than 0");
        }
    }

    /** Example: "Tech Fest 2026" -> "tech-fest-2026-a3f9" */
    private String generateUniqueSlug(String title) {
        String base = title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (base.isBlank()) {
            base = "event";
        }
        String slug;
        do {
            String suffix = String.format("%04x", ThreadLocalRandom.current().nextInt(0x10000));
            slug = base + "-" + suffix;
        } while (eventRepository.existsBySlug(slug));
        return slug;
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private EventResponse toResponse(Event event) {
        User organizer = event.getOrganizer();
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .venue(event.getVenue())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .capacity(event.getCapacity())
                .slug(event.getSlug())
                .publicUrl(publicEventLinks.forSlug(event.getSlug()))
                .posterUrl(event.getPosterUrl())
                .status(event.getStatus())
                .organizerId(organizer.getId())
                .organizerName(organizer.getName())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
