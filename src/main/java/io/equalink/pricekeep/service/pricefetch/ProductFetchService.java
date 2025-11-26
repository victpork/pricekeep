package io.equalink.pricekeep.service.pricefetch;

import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.repo.ProductRepo;
import io.equalink.pricekeep.repo.StoreRepo;
import io.quarkus.arc.All;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.virtual.threads.VirtualThreads;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.concurrent.ExecutorService;

@ApplicationScoped
public class ProductFetchService {

    @Inject
    @VirtualThreads
    private ExecutorService executor;


    @Inject
    private StoreRepo storeRepo;

    @Inject
    @All
    private List<InstanceHandle<ProductQuoteFetchService>> quoteProviders;

    @Inject
    private ProductRepo productRepo;

    public Multi<Quote> getProductQuoteFromExternalServices(String keyword, List<String> services, int page, int size) {


        // Selecting services that are on the name list
        var flowsOnList = quoteProviders.stream()
                              .filter(h -> services.contains(h.getBean().getName()))
                              .map(InstanceHandle::get)
                              .map(s -> s.fetchProductQuote(keyword))
                              .toList();

        return Multi.createBy().merging().streams(flowsOnList).runSubscriptionOn(executor);

    }

    public Multi<Quote> getProductQuoteFromExternalServices(String keyword, List<String> services) {
        // Selecting services that are on the name list
        var listOfFLows = quoteProviders.stream()
                              .filter(h -> services.contains(h.getBean().getName()))
                              .map(InstanceHandle::get)
                              .map(s -> s.fetchProductQuote(keyword))
                              .toList();

        // Merging the multi flows into single one, and create subscription and write to DB

        return Multi.createBy().merging().streams(listOfFLows);


    }

    public List<String> getListOfAvailableServices() {
        return quoteProviders.stream().map(s -> s.getBean().getName()).toList();
    }
}
