package io.equalink.pricekeep.service.pricefetch.paknsave;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class PakNSaveProductQuote {

    private String productId;
    private String brand;
    private String name;
    private String displayName;
    private PnsPricingStruct singlePrice;
}
