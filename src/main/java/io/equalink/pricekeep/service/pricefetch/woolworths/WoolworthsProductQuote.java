package io.equalink.pricekeep.service.pricefetch.woolworths;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class WoolworthsProductQuote {
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PriceInfo {
        BigDecimal originalPrice;
        BigDecimal salePrice;
        BigDecimal savePrice;
        BigDecimal savePercentage;
        boolean isClubPrice;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Multibuy {
        BigDecimal quantity;
        BigDecimal value;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SizeInfo {
        String cupMeasure;
        BigDecimal cupListPrice;
        BigDecimal cupPrice;
        String volumeSize;
        String packageType;
    }

    private String type;
    private String name;
    private String barcode;
    private String brand;
    private String sku;
    private String unit;
    private PriceInfo price;
    private Map<String, String> images;

    private SizeInfo size;

    private Multibuy multibuy;

    @JsonSetter("productTag")
    void extractMultibuy(JsonNode productTagNode) {
        if (productTagNode != null && productTagNode.has("multiBuy") && productTagNode.get("multiBuy").isObject()) {
            JsonNode multibuyNode = productTagNode.get("multiBuy");
            Multibuy mb = new Multibuy();
            if (multibuyNode.has("quantity")) {
                mb.setQuantity(multibuyNode.get("quantity").decimalValue());
            }
            if (multibuyNode.has("value")) {
                mb.setValue(multibuyNode.get("value").decimalValue());
            }
            this.multibuy = mb;
        }
    }
}
