package io.equalink.pricekeep.service.dto;

import jakarta.validation.constraints.NotNull;

public record LocationPoint(@NotNull long lat, @NotNull long lon) {

}
