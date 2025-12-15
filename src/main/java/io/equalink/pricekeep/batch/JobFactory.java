package io.equalink.pricekeep.batch;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;

@ApplicationScoped
public class JobFactory {

    @Produces
    StoreImportJob createStoreImportJob() {
        return new StoreImportJob();
    }

    @Produces
    ProductQuoteImportJob createProductQuoteImportJob() {
        return new ProductQuoteImportJob();
    }

}
