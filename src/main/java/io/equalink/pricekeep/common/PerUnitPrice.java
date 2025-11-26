package io.equalink.pricekeep.common;

import io.equalink.pricekeep.data.Quote;

import java.math.BigDecimal;

@FunctionalInterface
public interface PerUnitPrice {
    BigDecimal getPerUnitPrice(Quote q);
}
