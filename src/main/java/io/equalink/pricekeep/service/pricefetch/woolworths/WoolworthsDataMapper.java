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

    default GroupProductCode generateInternalProductCodeLinkage(WoolworthsProductQuote pq) {
        return GroupProductCode.builder().internalCode(pq.getSku()).build();
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
    @AfterMapping
    default void afterMappingProduct(@MappingTarget Product p, WoolworthsProductQuote pq, Store store) {
        String unitOfMeasureStr = pq.getSize().getCupMeasure();
        if (unitOfMeasureStr != null) {
            Matcher m = unitNumberPattern.matcher(unitOfMeasureStr);
            if (m.find()) {
                String unit = m.group("unit");
                p.setUnit(switch (unit.toLowerCase()) {
                    case "ea" -> Product.Unit.PER_ITEM;
                    case "kg" -> Product.Unit.PER_KG;
                    case "m" -> Product.Unit.PER_METRE;
                    case "ml" -> Product.Unit.PER_MILLILITRE;
                    case "l" -> Product.Unit.PER_LITRE;
                    case "g" -> Product.Unit.PER_G;
                    default -> null;
                });
                p.setUnitScale(new BigDecimal(m.group("number")));
            }
        }
        // Determine product size and items in package
        if (pq.getSize().getVolumeSize() != null && !pq.getSize().getVolumeSize().isEmpty()) {
            String volSize = pq.getSize().getVolumeSize().toLowerCase();
            int packPos = volSize.indexOf("pack");
            if (packPos >= 0) {
                //Packaged product with multiple containers
                p.setItemPerPackage(Integer.parseInt(volSize.substring(0, packPos)));
                if (pq.getSize().getPackageType() != null && !pq.getSize().getPackageType().isEmpty()) {
                    String packageType = pq.getSize().getPackageType().toLowerCase();
                    Matcher m = unitNumberPattern.matcher(packageType);
                    if (m.find()) {
                        p.setPackageSize(new BigDecimal(m.group("number")));
                    } else {
                        Log.warnv("Cannot determine volume for {0} with string {1} ", pq.getName(), pq.getSize().getPackageType());
                    }
                } else {
                    Log.warnv("Cannot determine volume size for product {0}, volumeSize is empty", pq.getName());
                }
            } else {
                //Product in single container
                p.setItemPerPackage(1);
                Matcher m = unitNumberPattern.matcher(volSize);
                if (m.find()) {
                    p.setPackageSize(new BigDecimal(m.group("number")));
                } else {
                    Log.warnv("Cannot determine volume for {0} with string {1} ", pq.getName(), pq.getSize().getVolumeSize());
                }
            }
        }
        var intPrdCode = generateInternalProductCodeLinkage(pq);
        intPrdCode.setProduct(p);
        intPrdCode.setStoreGroup(store.getGroup());
        p.addSKUMapping(intPrdCode);
    }

    default StoreGroup getWWStoreGroup() {
        var wwSG = new StoreGroup();
        wwSG.setId(1L);
        wwSG.setName("Woolworths");
        return wwSG;
    }
}
