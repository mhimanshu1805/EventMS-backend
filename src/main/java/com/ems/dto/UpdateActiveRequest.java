package com.ems.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateActiveRequest {

    @NotNull(message = "active is required")
    private Boolean active;
}
