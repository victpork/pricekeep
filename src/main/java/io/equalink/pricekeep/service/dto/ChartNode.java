package io.equalink.pricekeep.service.dto;

import jakarta.validation.constraints.NotNull;

public record ChartNode(@NotNull String index, @NotNull String value) {

}
