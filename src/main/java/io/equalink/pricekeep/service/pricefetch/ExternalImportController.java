package io.equalink.pricekeep.service.pricefetch;

import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.data.StoreGroup;
import io.equalink.pricekeep.repo.ProductRepo;
import io.equalink.pricekeep.repo.StoreRepo;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.virtual.threads.VirtualThreads;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.jbosslog.JBossLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@ApplicationScoped
@JBossLog
public class ExternalImportController {

    @Inject
    @VirtualThreads
    private ExecutorService executor;


    @Inject
    private StoreRepo storeRepo;

    @Inject
    Instance<ProductQuoteFetchService> pqFetchServiceInstance;

    @Inject
    Instance<StoreFetchService> sFetchServiceInstance;

    private Map<String, ProductQuoteFetchService> quoteProviders;

    private Map<String, StoreFetchService> storeProviders;

    @Inject
    private ProductRepo productRepo;


    @PostConstruct
    public void init() {
        this.quoteProviders = Arc.container()
                                  .listAll(ProductQuoteFetchService.class)
                                  .stream()
                                  .collect(Collectors.toMap(
                                      this::<ProductQuoteFetchService>extractName,
                                      InstanceHandle::get
                                  ));

        this.storeProviders = Arc.container()
                                  .listAll(StoreFetchService.class)
                                  .stream()
                                  .collect(Collectors.toMap(
                                      this::<StoreFetchService>extractName,
                                      InstanceHandle::get
                                  ));
    }

    /**
     * Extract available instances which implements the supplied interface and return the name of the class
     * @param handle
     * @return
     * @param <T>
     */
    private <T> String extractName(InstanceHandle<T> handle) {
        Class<?> beanClass = handle.getBean().getBeanClass();

        // Try @Named first
        Named named = beanClass.getAnnotation(Named.class);
        if (named != null && !named.value().isEmpty()) {
            return named.value();
        }

        // Try @Identifier
        io.smallrye.common.annotation.Identifier identifier =
            beanClass.getAnnotation(io.smallrye.common.annotation.Identifier.class);
        if (identifier != null && !identifier.value().isEmpty()) {
            return identifier.value();
        }

        throw new IllegalStateException(
            "Bean " + beanClass.getName() +
                " must have @Named or @Identifier annotation"
        );
    }

    @ActivateRequestContext
    public Multi<Quote> getProductQuoteFromExternalServices(String keyword, List<Store> sources) {
        log.infov("Content of storeProviders: {0}", storeProviders);
        // Step 1: Group stores by group name
        // Step 2: Transform each group into a Multi by finding the matching service provider
        // Step 3: Merge all Multi streams into a single Multi
        return Multi.createFrom().iterable(sources).group().by(
                s -> s.getGroup().getName()).onItem()
                   .transformToMulti(group -> {
                       String serviceName = group.key();
                       ProductQuoteFetchService service = quoteProviders.get(serviceName);
                       if (service == null) {
                           log.errorv("Service {0} not found", serviceName);
                           return Multi.createFrom().empty();
                       }
                       return group.onItem()
                                  .transformToMulti(s -> service.fetchProductQuote(s, keyword))
                                  .merge();
                   }).merge();

    }

    public List<String> getListOfAvailableServices() {
        return quoteProviders.keySet().stream().toList();
    }

    public Multi<Store> getStoreListFromExternalServices(List<StoreGroup> storeGroup) {
        log.infov("Content of storeProviders: {0}", storeProviders);
        return Multi.createFrom().iterable(storeGroup).onItem()
                   .transform(sg -> storeProviders.get(sg.getName())
                                        .fetchStore()
                                        .onItem()
                                        .transform(s -> {
                                            s.setGroup(sg);
                                            return s;
                                        })).flatMap(m -> m);
    }
}
