package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.batch.JobStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record
JobInfo(Long id, boolean enabled, String name, String description, String type, String frequency, JobStatus status, JobStatus lastResult, LocalDateTime lastRunTime, LocalDateTime nextExecTime, Map<String, String> parameters) {
}
