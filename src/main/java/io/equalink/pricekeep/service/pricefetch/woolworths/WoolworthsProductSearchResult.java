package io.equalink.pricekeep.service.pricefetch.woolworths;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class WoolworthsProductSearchResult {
    ProductWrapper products;
    int currentPageSize;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter @Setter
    public static class ProductWrapper {
        List<WoolworthsProductQuote> items;
        int totalItems;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter @Setter
    public static class Context {
        Map<String, String> fulfillment;
    }
}
