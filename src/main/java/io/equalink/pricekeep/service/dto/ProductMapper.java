package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.data.Quote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.equalink.pricekeep.data.Product;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi", uses = {StoreMapper.class})
public interface ProductMapper {
    @Mapping(target = "desc", source = "description", defaultValue = "")
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
    @Mapping(target = "unitScale", ignore = true)
    public Product toEntity(ProductInfo pDTO);

    @Mapping(target = "productInfo", source = "product")
    @Mapping(target = "storeInfo", source = "quoteStore")
    @Mapping(target = "quoteDate", source = "quoteDate")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "discountType", source = "discount.type")
    @Mapping(target = "discountPrice", source = "discount.salePrice")
    @Mapping(target = "multibuyQuantity", source = "discount.multiBuyQuantity")
    public QuoteDTO toQuoteDTO(Quote q);

    @Mapping(target = "storeInfo", source = "q")
    @Mapping(target = "productInfo", ignore = true)
    @Mapping(target = "discountType", ignore = true)
    @Mapping(target = "discountPrice", ignore = true)
    @Mapping(target = "multibuyQuantity", ignore = true)
    public QuoteDTO toQuoteDTO(CompactQuote q);

    @Mapping(target = "storeInfo", source = "quoteStore")
    @Mapping(target = "quoteDate", source = "quoteDate")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "discountType", source = "discount.type")
    @Mapping(target = "unit", expression = "java((q.getProduct().getUnitScale() == null ? \"\" : q.getProduct().getUnitScale().stripTrailingZeros().toPlainString()) + q.getProduct().getUnit().code)")
    @Mapping(target = "discountPrice", source = "discount.salePrice")
    @Mapping(target = "multibuyQuantity", source = "discount.multiBuyQuantity")
    public SimpleQuoteDTO toSimpleQuoteDTO(Quote q);
}
