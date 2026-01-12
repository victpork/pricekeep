package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.data.Discount;
import io.equalink.pricekeep.data.Product;
import io.equalink.pricekeep.data.Quote;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "cdi", uses = {StoreMapper.class})
public interface QuoteMapper {

    @Mapping(target = "quoteStore", ignore = true)
    @Mapping(target = "quoteSource", constant = "USER")
    @Mapping(target = "discount", source = "qInfo")
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "discountedUnitPrice", ignore = true)
    Quote toEntity(QuoteDTO qInfo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quote", ignore = true)
    @Mapping(target = "type", source = "discountType")
    @Mapping(target = "multiBuyQuantity", source = "multibuyQuantity",
        conditionExpression = "java(d.discountType == Discount.Type.BUNDLE)")
    @Mapping(target = "saveValue", ignore = true)
    @Mapping(target = "salePrice", source = "discountPrice")
    Discount toDiscountEntity(QuoteDTO d);
}
