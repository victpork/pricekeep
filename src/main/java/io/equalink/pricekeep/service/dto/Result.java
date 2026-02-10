package io.equalink.pricekeep.service.dto;

import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Result message", name = "Result")
@Builder
public record Result(String result, String msg) {
}
