package io.equalink.pricekeep.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.equalink.pricekeep.data.Discount;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SimpleQuoteDTO(
        StoreInfo storeInfo,

        @NotNull
        @Schema(required = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate quoteDate,
        @NotNull
        @Schema(required = true)
        BigDecimal price,
        @NotNull
        @Schema(required = true)
        BigDecimal unitPrice,
        String unit,
        Discount.Type discountType,
        BigDecimal discountPrice,
        BigDecimal multibuyQuantity
) {
}
