package com.ems.dto;

import com.ems.entity.RegistrationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationStatusRequest {

    @NotNull(message = "Status is required")
    private RegistrationStatus status;
}
