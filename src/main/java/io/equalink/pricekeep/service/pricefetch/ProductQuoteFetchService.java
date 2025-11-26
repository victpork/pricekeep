package io.equalink.pricekeep.service.pricefetch;

import io.equalink.pricekeep.data.Quote;
import io.smallrye.mutiny.Multi;

public interface ProductQuoteFetchService {
    Multi<Quote> fetchProductQuote(String keyword);
}
