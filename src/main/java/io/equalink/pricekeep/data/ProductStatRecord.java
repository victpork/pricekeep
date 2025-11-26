package io.equalink.pricekeep.data;

import java.math.BigDecimal;

public record ProductStatRecord(
    Long productId,
    BigDecimal monthMin,
    BigDecimal monthAvg,
    BigDecimal quarterMin,
    BigDecimal quarterAvg,
    BigDecimal yearMin,
    BigDecimal yearAvg
) {}
