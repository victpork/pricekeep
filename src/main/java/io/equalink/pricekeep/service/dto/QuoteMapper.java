package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.data.Quote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi", uses={StoreMapper.class})
public interface QuoteMapper {

    @Mapping(target = "quoteStore", ignore = true)
    @Mapping(target = "quoteSource", constant = "USER")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "discount", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "discountedUnitPrice", ignore = true)
    Quote toEntity(QuoteDTO qInfo);

    @Mapping(target = "index", source = "quoteDate")
    @Mapping(target = "value", source = "price")
    ChartNode toChartNode(Quote q);
}
