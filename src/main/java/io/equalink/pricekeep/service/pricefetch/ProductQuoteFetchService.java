package io.equalink.pricekeep.service.pricefetch;

import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import io.smallrye.mutiny.Multi;

public interface ProductQuoteFetchService {
    Multi<Quote> fetchProductQuote(Store store, String keyword);
}
