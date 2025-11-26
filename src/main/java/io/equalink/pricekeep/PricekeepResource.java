package io.equalink.pricekeep;

import io.equalink.pricekeep.service.StoreService;
import io.equalink.pricekeep.service.dto.*;
import io.equalink.pricekeep.service.quote.ProductService;
import io.equalink.pricekeep.service.quote.QuoteService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/common")
@Consumes(MediaType.APPLICATION_JSON)
public class PricekeepResource {

    @Inject
    QuoteService quoteService;

    @Inject
    ProductService productService;

    @Inject
    StoreService storeService;

    @Inject
    QuoteMapper qm;

    @Inject
    StoreMapper sm;

    @GET
    @Path("/latestDeals")
    public QuoteResult getRecentDiscount(@QueryParam("s") @DefaultValue(value = "5") int pageSize) {
        return new QuoteResult(productService.latestDeals(1, pageSize));
    }

}
    