package io.equalink.pricekeep.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.equalink.pricekeep.data.Product;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductInfo(
    @Schema(readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long id,

    @NotEmpty
    @Schema(required = true)
    String name,
    String imgUrl,
    String desc,
    @Schema(maxLength = 14, minLength = 8)
    @Pattern(regexp = "^([0-9]d{8}|[0-9]{12}|[0-9]{13}|[0-9]{14})$", message = "GTIN must be numeric and must be either having 8, 12, 13 or 14 digits")
    String gtin,

    @Schema(required = true, defaultValue = "PER_UNIT")
    Product.Unit unit,

    @Schema(readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    List<SimpleQuoteDTO> latestQuotes,

    @Schema(readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Map<String, BigDecimal> stats,

    BigDecimal quantityPerItem,

    Integer itemPerBundle,

    Set<String> tags
) {
}
