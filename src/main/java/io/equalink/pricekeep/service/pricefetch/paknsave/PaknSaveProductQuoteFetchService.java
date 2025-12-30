package io.equalink.pricekeep.service.pricefetch.paknsave;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Page;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.service.pricefetch.BaseScraper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.GeneratorEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.util.List;

@ApplicationScoped
@Named("paknsave")
public class PaknSaveProductQuoteFetchService extends BaseScraper<PakNSaveProductQuote> {

    @Inject
    ObjectMapper objMapper;

    @Inject
    @Named("withProxy")
    Page page;

    private static final String API_URL = "https://api-prod.paknsave.co.nz/v1/edge/search/paginated/products";

    private static final String PRODUCT_URL = "https://www.paknsave.co.nz/shop/search?pg=1&q=";
    public PaknSaveProductQuoteFetchService() {
        
    }

    @Override
    protected void initSearch(String keyword, GeneratorEmitter<? super List<PakNSaveProductQuote>> em) {
        page.unrouteAll();

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
