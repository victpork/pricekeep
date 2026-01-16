package io.equalink.pricekeep.service.dto;

import java.math.BigDecimal;

public record AlertDTO(Long productId, BigDecimal priceLevel) {
}
