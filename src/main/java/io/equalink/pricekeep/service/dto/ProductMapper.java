package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.data.Quote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.equalink.pricekeep.data.Product;

@Mapper(componentModel = "cdi", uses = {StoreMapper.class})
public interface ProductMapper {
    @Mapping(target = "desc", source = "description")
    @Mapping(target = "stats", source = "priceStats")
    @Mapping(target = "imgUrl", source = "imgPath")
    @Mapping(target = "latestQuotes", source = "priceQuotes")
    @Mapping(target = "quantityPerItem", source = "packageSize")
    @Mapping(target = "itemPerBundle", source = "itemPerPackage")
    public ProductInfo toDTO(Product p);

    @Mapping(target = "description", source = "desc")
    @Mapping(target = "priceQuotes", ignore = true)
    @Mapping(target = "groupSKU", ignore = true)
    @Mapping(target = "unit", source = "unit")
    @Mapping(target = "priceStats", ignore = true)
    @Mapping(target = "packageSize", source = "quantityPerItem")
    @Mapping(target = "itemPerPackage", source = "itemPerBundle")
    @Mapping(target = "imgPath", source = "imgUrl")
    public Product toEntity(ProductInfo pDTO);

    @Mapping(target = "productInfo", source = "product")
    @Mapping(target = "storeInfo", source = "quoteStore")
    @Mapping(target = "quoteDate", source = "quoteDate")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "discountType", source = "discount.type")
    @Mapping(target = "id", ignore = true)
    public QuoteDTO toQuoteDTO(Quote q);

    @Mapping(target="id", ignore = true)
    @Mapping(target="storeInfo", source = "q")
    @Mapping(target="productInfo", ignore = true)
    @Mapping(target="discountType", ignore = true)
    public QuoteDTO toQuoteDTO(CompactQuote q);

    @Mapping(target = "storeInfo", source = "quoteStore")
    @Mapping(target = "quoteDate", source = "quoteDate")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "discountType", source = "discount.type")
    @Mapping(target = "id", ignore = true)
    public SimpleQuoteDTO toSimpleQuoteDTO(Quote q);
}
