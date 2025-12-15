package io.equalink.pricekeep.service.pricefetch.paknsave;

import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.service.pricefetch.BaseScraper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.GeneratorEmitter;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.util.List;

@ApplicationScoped
public class PaknSaveDataProvider extends BaseScraper<PakNSaveProductQuote> {

    private static final String PRODUCT_URL = "https://www.paknsave.co.nz/shop/search?pg=1&q=";
    public PaknSaveDataProvider() {
        
    }

    @Override
    protected void initSearch(String keyword, GeneratorEmitter<? super List<PakNSaveProductQuote>> em) {

    }

    @Override
    protected Quote mapperFunction(PakNSaveProductQuote item) {
        return null;
    }

    @Override
    protected List<PakNSaveProductQuote> fetchNext() throws IOException {
        return List.of();
    }

    @Override
    public Uni<Void> setStore(String storeInternalCode) {
        return null;
    }

    @Override
    public Multi<Store> fetchStore() {
        return null;
    }

    @Override
    public Multi<Quote> fetchProductQuote(Store store, String keyword) {
        return null;
    }
}
