package com.ems.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventStatsResponse {

    private Long eventId;
    private int capacity;
    private long totalRegistrations;
    private long confirmed;
    private long cancelled;
    private long remainingCapacity;
}
