package io.equalink.pricekeep.service.pricefetch.paknsave;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PnsPricingStruct {
    private BigDecimal price;
    private UnitPrice comparativePrice;


    @Data
    static class UnitPrice {
        private String unitQuantityUom;
        private Integer pricePerUnit;
        private Integer unitQuantity;
        private String measureDescription;
    }


    private List<PnsPromotion> promotions;
}
