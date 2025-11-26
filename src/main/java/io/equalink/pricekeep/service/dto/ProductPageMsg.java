package io.equalink.pricekeep.service.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record ProductPageMsg(@NotNull String name, String description, List<QuoteDTO> quotes) {

}
