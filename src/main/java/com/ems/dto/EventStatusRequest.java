package com.ems.dto;

import com.ems.entity.EventStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventStatusRequest {

    @NotNull(message = "Status is required")
    private EventStatus status;
}
