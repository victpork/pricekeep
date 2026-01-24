package io.equalink;

import com.microsoft.playwright.Playwright;
import io.equalink.pricekeep.data.Product;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.data.StoreGroup;
import io.equalink.pricekeep.repo.ProductRepo;
import io.equalink.pricekeep.repo.StoreRepo;
import io.equalink.pricekeep.service.pricefetch.ExternalImportController;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class DataFetchTest {

    private static final Logger LOG = Logger.getLogger(DataFetchTest.class);

    @Inject
    ExternalImportController fetchService;

    @Inject
    StoreRepo storeRepo;

    @Inject
    ProductRepo productRepo;

    @ConfigProperty(name = "proxyAddr")
    String proxyAddr;

    @BeforeAll
    static void init() {
        Playwright.create();
    }

    @Test
    void testProductFetchService() {
        var serviceList = fetchService.getListOfAvailableServices();
        LOG.info(serviceList.getFirst());
        assertEquals(1, serviceList.size());
    }

    @Test
    void testFetchStore() {
        Optional<StoreGroup> sg = storeRepo.findStoreGroupByName("woolworths");
        assertTrue(sg.isPresent());
        var storeStream = fetchService.getStoreListFromExternalServices(List.of(sg.get()));
        storeStream.subscribe().with(s -> storeRepo.persist(s));
    }



    @Test
    void testFetchAll() throws Exception {
        assertEquals("localhost:3041", proxyAddr);
        Optional<StoreGroup> sg = storeRepo.findStoreGroupByName("woolworths");
        assertTrue(sg.isPresent());
        Store s = new Store();
        s.setGroup(sg.get());
        s.setName("Woolworths Hobsonville");
        s.setInternalId("1223854");
        s.setAddress("124 Hobsonville Road,Hobsonville Click Collect,0618,Hobsonville Click and Collect");
        storeRepo.persist(s);
        assertNotNull(s.getId());

        CompletableFuture<Void> syncFlag = new CompletableFuture<>();
        List<Quote> res = new ArrayList<>();
        fetchService.getProductQuoteFromExternalServices("cheese", List.of(s))
            .subscribe().with(res::add, () -> syncFlag.complete(null));
        syncFlag.join();
        res.forEach(q -> {
            // Check if product already exists in DB
            Product p = q.getProduct();

            if (productRepo.findByGTIN(p.getGtin()).isEmpty()) {
                productRepo.persist(p);
            }
            var prdGrpRel = p.getGroupSKU().stream().findFirst();
            prdGrpRel.ifPresent(rel -> {
                var gpCodePresent = productRepo.findBySKUOrGTIN(null, rel.getInternalCode(), rel.getStoreGroup().getName());
                if (gpCodePresent.isEmpty()) {
                    productRepo.persist(p);
                }
            });
            q.setQuoteStore(s);
            productRepo.persist(q);
        });
        //assertThat(res, hasSize(512));
    }
}
