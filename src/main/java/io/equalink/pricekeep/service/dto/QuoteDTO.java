package io.equalink.pricekeep.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.equalink.pricekeep.data.Discount;
import io.equalink.pricekeep.service.dto.utils.StoreInfoTypeDeserializer;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema
@Getter
@Setter
@AllArgsConstructor
public class QuoteDTO {
    @Schema(readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long id;

    ProductInfo productInfo;

    @NotNull
    @Schema(
        anyOf = {
            StoreInfo.class,
            BaseEntity.WithId.class
        },
        implementation = Void.class
    )

    @JsonDeserialize(using = StoreInfoTypeDeserializer.class)
    BaseEntity<StoreInfo> storeInfo;

    @NotNull
    @Schema(required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate quoteDate;
    @NotNull
    @Schema(required = true)
    BigDecimal price;

    Discount.Type discountType;
    BigDecimal salePrice;
    Integer multibuyQuantity;
}
