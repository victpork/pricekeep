package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.data.Quote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.equalink.pricekeep.data.Product;

@Mapper(componentModel = "cdi", uses = {StoreMapper.class})
public interface ProductMapper {
    @Mapping(target = "desc", source = "description")
    @Mapping(target = "stats", source = "priceStats")
    @Mapping(target = "imgUrl", ignore = true)
    @Mapping(target = "latestQuotes", source = "priceQuotes")
    public ProductInfo toDTO(Product p);

    @Mapping(target = "description", source = "desc")
    @Mapping(target = "priceQuotes", ignore = true)
    @Mapping(target = "groupSKU", ignore = true)
    @Mapping(target = "unit", source = "unit")
    @Mapping(target = "priceStats", ignore = true)
    public Product toEntity(ProductInfo pDTO);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "productInfo", source = "product")
    @Mapping(target = "storeInfo", source = "quoteStore")
    @Mapping(target = "quoteDate", source = "quoteDate")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "discountType", source = "discount.type")
    public QuoteDTO toQuoteDTO(Quote q);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "storeInfo", source = "quoteStore")
    @Mapping(target = "quoteDate", source = "quoteDate")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "discountType", source = "discount.type")
    public SimpleQuoteDTO toSimpleQuoteDTO(Quote q);
}
