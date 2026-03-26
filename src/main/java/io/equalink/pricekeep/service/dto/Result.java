package io.equalink.pricekeep.service.dto;

import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Result message", name = "Result")
@Builder
public record Result(String result, String msg, Object entity) {

    public Result {
        Objects.requireNonNull(result);
    }

    public Result(String result, String msg) {
        this(result, msg, null);
    }
}
