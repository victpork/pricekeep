package io.equalink.pricekeep.service.pricefetch.paknsave;

import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.service.pricefetch.BaseScraper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.util.List;

@ApplicationScoped
public class PaknSaveDataProvider extends BaseScraper<PakNSaveProductQuote> {

    private static final String PRODUCT_URL = "https://www.paknsave.co.nz/shop/search?pg=1&q=";
    public PaknSaveDataProvider() {
        
    }

    @Override
    protected void initSearch(String keyword) {

    }

    @Override
    protected Quote mapperFunction(PakNSaveProductQuote item) {
        return null;
    }

    @Override
    protected List<PakNSaveProductQuote> fetchItem() throws IOException {
        return List.of();
    }

    @Override
    public Uni<Void> setStore(Store store) {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<List<Store>> getStoreList() {
        return Uni.createFrom().item(List.of());
    }
}
