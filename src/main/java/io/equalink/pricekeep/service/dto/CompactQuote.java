package io.equalink.pricekeep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CompactQuote {
    private String storeName;
    private LocalDate quoteDate;
    private BigDecimal price;
    private BigDecimal dctUnitPrice;
    private String quoteSource;
    private BigDecimal unitPrice;
    private Long discountId;
    private Long productId;
    private Long storeId;
}
