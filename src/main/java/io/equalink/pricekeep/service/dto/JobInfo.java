package io.equalink.pricekeep.service.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record JobInfo(Long id, boolean enabled, String name, String description, LocalDateTime lastRunTime, LocalDateTime nextExecTime) {
}
