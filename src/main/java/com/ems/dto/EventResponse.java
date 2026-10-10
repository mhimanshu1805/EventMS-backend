package com.ems.dto;

import java.time.LocalDateTime;

import com.ems.entity.EventStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private String venue;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer capacity;
    private String slug;
    /** Public page address. Computed from the slug; not stored on the event. */
    private String publicUrl;
    private String posterUrl;
    private EventStatus status;
    private Long organizerId;
    private String organizerName;
    private LocalDateTime createdAt;
}
