package io.equalink.pricekeep.service.dto;

import java.math.BigDecimal;

public record ProductSearchDTO(String name, String desc, String src, BigDecimal orginalPrice, BigDecimal dctPrice) {

}
