package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.data.Product;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = "cdi", uses={StoreMapper.class})
public interface QuoteMapper {

    @Mapping(target = "quoteStore", ignore = true)
    @Mapping(target = "quoteSource", constant = "USER")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "discount", ignore = true)
    @Mapping(target = "product", ignore = true)
    Quote toEntity(QuoteDTO qInfo);

    @Mapping(target = "index", source = "quoteDate")
    @Mapping(target = "value", source = "price")
    ChartNode toChartNode(Quote q);
}
