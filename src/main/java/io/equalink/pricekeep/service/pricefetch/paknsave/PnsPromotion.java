package io.equalink.pricekeep.service.pricefetch.paknsave;

import lombok.Data;

@Data
public class PnsPromotion {
    /**
     * Either the price on price tag or the total price if multibuy = true
     */
    Integer rewardValue;
    /**
     *
     */
    String sapType;
    String rewardType;
    /**
     * Amount of item need to buy to satisify the multibuy promo
     */
    Integer threshold;
    /**
     * is multibuy promotion
     */
    Boolean multiProducts;

}
