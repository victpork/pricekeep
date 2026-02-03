package io.equalink.pricekeep.service.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Result message", name = "Result")
public record Result(String result, String msg) {
}
