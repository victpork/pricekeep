package io.equalink.pricekeep.service.pricefetch;

import io.equalink.pricekeep.data.Store;
import io.smallrye.mutiny.Multi;

public interface StoreFetchService {
    Multi<Store> fetchStore();
}
