package com.ems.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dto.QueryRequest;
import com.ems.dto.QueryResponse;
import com.ems.entity.Event;
import com.ems.entity.Query;
import com.ems.entity.QueryStatus;
import com.ems.entity.User;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.EventRepository;
import com.ems.repository.QueryRepository;
import com.ems.repository.UserRepository;

@Service
@Transactional
public class QueryService {

    private final QueryRepository queryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    public QueryService(
            QueryRepository queryRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            EventService eventService) {
        this.queryRepository = queryRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public QueryResponse create(Long eventId, QueryRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        Query saved = queryRepository.save(Query.builder()
                .event(event)
                .participant(currentUser())
                .message(request.getMessage().trim())
                .status(QueryStatus.OPEN)
                .build());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<QueryResponse> listMine() {
        return queryRepository.findByParticipantOrderByCreatedAtDesc(currentUser()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QueryResponse> listForOwnedEvent(Long eventId) {
        Event event = eventService.requireOwnedEvent(eventId);
        return queryRepository.findByEventOrderByCreatedAtDesc(event).stream()
                .map(this::toResponse)
                .toList();
    }

    public QueryResponse respond(Long id, String responseText) {
        Query query = queryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found"));
        eventService.requireOwnedEvent(query.getEvent().getId());
        query.setResponse(responseText.trim());
        query.setStatus(QueryStatus.ANSWERED);
        query.setRespondedAt(LocalDateTime.now());
        return toResponse(queryRepository.save(query));
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private QueryResponse toResponse(Query query) {
        Event event = query.getEvent();
        User participant = query.getParticipant();
        return QueryResponse.builder()
                .id(query.getId())
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .participantId(participant.getId())
                .participantName(participant.getName())
                .participantEmail(participant.getEmail())
                .message(query.getMessage())
                .response(query.getResponse())
                .status(query.getStatus())
                .createdAt(query.getCreatedAt())
                .respondedAt(query.getRespondedAt())
                .build();
    }
}
