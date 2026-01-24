package io.equalink.pricekeep.service.pricefetch.woolworths;


import io.equalink.pricekeep.data.*;
import io.quarkus.logging.Log;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mapper(componentModel = "cdi")
public interface WoolworthsDataMapper {

    //Pattern itemCountPattern = Pattern.compile("([0-9]+)");

    Pattern unitNumberPattern = Pattern.compile("(?<number>\\d+(?:\\.\\d+)?)\\s*(?<unit>ml|l|cm|m|g|kg|ea)");

    @Mapping(target = "priceStats", ignore = true)
    @Mapping(target = "priceQuotes", ignore = true)
    @Mapping(target = "packageSize", ignore = true)
    @Mapping(target = "itemPerPackage", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gtin", source = "pq.barcode")
    @Mapping(target = "groupSKU", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "name", expression = "java(generateName(pq))")
    @Mapping(target = "unitScale", ignore = true)
    @Mapping(target = "imgPath", ignore = true)
    @Mapping(target = "unit", ignore = true)
    Product toProduct(WoolworthsProductQuote pq);

    @Mapping(target = "quoteStore", ignore = true)
    @Mapping(target = "quoteSource", constant = "SYSTEM")
    @Mapping(target = "quoteDate", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "product", source = "pq")
    @Mapping(target = "discount", source = "pq")
    @Mapping(target = "price", source = "pq.price.originalPrice")
    @Mapping(target = "unitPrice", source = "pq.size.cupListPrice")
    @Mapping(target = "discountedUnitPrice", source = "pq.size.cupPrice")
    Quote toQuote(WoolworthsProductQuote pq);

    //@Mapping(target = "geoPoint", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "internalId", source = "id")
    @Mapping(target = "url", ignore = true)
    @Mapping(target = "name", expression = "java(addr.getName().trim())")
    @Mapping(target = "address", expression = "java(addr.getAddress().trim())")
    @Mapping(target = "group", expression = "java(getWWStoreGroup())")
    Store toStore(WoolworthsStoreAddress addr);

    default String generateName(WoolworthsProductQuote pq) {
        String packageType = Optional.ofNullable(pq.getSize().getPackageType()).orElse("");
        String volumeSize = Optional.ofNullable(pq.getSize().getVolumeSize()).orElse("");
        return String.join(" ", pq.getName().trim(), packageType, volumeSize);
    }

    default Discount toDiscount(WoolworthsProductQuote pq) {
        if (BigDecimal.ZERO.compareTo(pq.getPrice().getSavePrice()) == 0) return null;
        Discount res = new Discount();
        if (pq.getMultibuy() != null) {
            res.setType(Discount.Type.BUNDLE);
            res.setMultiBuyQuantity(pq.getMultibuy().getQuantity().intValue());
            res.setSaveValue(pq.getMultibuy().getValue());
            res.setSalePrice(pq.getMultibuy().getValue().divide(pq.getMultibuy().getQuantity(), 2, RoundingMode.HALF_EVEN));
        } else {
            var priceStruct = pq.getPrice();
            if (BigDecimal.ZERO.compareTo(priceStruct.getSavePercentage()) < 0) {
                res.setType(Discount.Type.PERCENTAGE);
                res.setSaveValue(priceStruct.getSavePercentage());
                res.setSalePrice(priceStruct.getSalePrice());
            } else if (BigDecimal.ZERO.compareTo(priceStruct.getSavePrice()) < 0){
                res.setType(Discount.Type.FIXED_AMOUNT);
                res.setSaveValue(priceStruct.getSavePrice());
                res.setSalePrice(priceStruct.getSalePrice());
            }
        }
        return res;
    }

    @AfterMapping
    default void afterMappingQuote(@MappingTarget Quote q, WoolworthsProductQuote pq) {
        if (q.getDiscount() != null) {
            q.getDiscount().setQuote(q);
        }
    }

    default StoreGroup getWWStoreGroup() {
        var wwSG = new StoreGroup();
        wwSG.setId(1L);
        wwSG.setName("Woolworths");
        return wwSG;
    }
}
