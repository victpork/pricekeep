package io.equalink.pricekeep.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.equalink.pricekeep.batch.JobStatus;
import io.smallrye.common.constraint.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record
JobInfo(@Schema(readOnly = true, required = true)
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @NotEmpty
        @NotNull
        @NotBlank
        Long id,
        boolean enabled,

        String name,
        String description,
        @Schema(readOnly = true)
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @NotEmpty
        String type,
        @Schema(readOnly = true)
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @NotEmpty
        String frequency,
        @Schema(readOnly = true)
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @NotEmpty
        JobStatus status,
        @Schema(readOnly = true)
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @NotEmpty
        JobStatus lastResult,
        @Schema(readOnly = true)
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @NotEmpty
        LocalDateTime lastRunTime,
        @Schema(readOnly = true)
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @NotEmpty
        LocalDateTime nextExecTime,
        Map<String, String> parameters) {
}
