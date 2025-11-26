package io.equalink.pricekeep.service.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;


public sealed interface BaseEntity<T> {

    record WithDetail<T>(@JsonUnwrapped T value) implements BaseEntity<T> {
    }

    @Schema(
        properties = {
            @SchemaProperty(name = "id", readOnly = true)
        },
        requiredProperties = {"id"}
    )
    record WithId<T>(@NotNull Long id) implements BaseEntity<T> {
    }
}
