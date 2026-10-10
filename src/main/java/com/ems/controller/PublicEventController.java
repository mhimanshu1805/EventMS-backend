package com.ems.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ems.dto.EventResponse;
import com.ems.service.EventService;

@RestController
@RequestMapping("/api/events/public")
public class PublicEventController {

    private final EventService eventService;

    public PublicEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponse> listPublished(@RequestParam(required = false) String search) {
        return eventService.listPublished(search);
    }

    @GetMapping("/{slug}")
    public EventResponse getBySlug(@PathVariable String slug) {
        return eventService.getPublishedBySlug(slug);
    }
}
