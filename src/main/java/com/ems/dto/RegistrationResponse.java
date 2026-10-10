package com.ems.dto;

import java.time.LocalDateTime;

import com.ems.entity.RegistrationStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistrationResponse {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private String eventSlug;
    private Long participantId;
    private String participantName;
    private String participantEmail;
    private String phone;
    private RegistrationStatus status;
    private LocalDateTime registeredAt;
}
