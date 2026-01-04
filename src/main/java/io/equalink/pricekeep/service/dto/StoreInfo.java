package io.equalink.pricekeep.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StoreInfo(
        @Schema(readOnly = true)
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,
        @NotNull
        @Schema(required = true)
        String name,
        String address,
        String storeGroupLogoPath,
        String url) {
}
