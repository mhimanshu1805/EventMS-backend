package com.ems.dto;

import java.time.LocalDateTime;

import com.ems.entity.QueryStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QueryResponse {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long participantId;
    private String participantName;
    private String participantEmail;
    private String message;
    private String response;
    private QueryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
