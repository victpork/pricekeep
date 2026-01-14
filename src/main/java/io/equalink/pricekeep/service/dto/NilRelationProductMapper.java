package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.data.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface NilRelationProductMapper {
    @Mapping(target = "desc", source = "description", defaultValue = "")
    @Mapping(target = "stats", ignore = true)
    @Mapping(target = "imgUrl", source = "imgPath")
    @Mapping(target = "latestQuotes", ignore = true)
    @Mapping(target = "quantityPerItem", source = "packageSize")
    @Mapping(target = "itemPerBundle", source = "itemPerPackage")
    ProductInfo toDTOWithoutRelation(Product p);
}
