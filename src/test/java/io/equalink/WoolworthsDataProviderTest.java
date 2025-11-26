package io.equalink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.Proxy;
import io.equalink.pricekeep.data.GroupProductCode;
import io.equalink.pricekeep.data.Product;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.repo.ProductRepo;
import io.equalink.pricekeep.repo.StoreRepo;
import io.equalink.pricekeep.service.pricefetch.woolworths.*;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class WoolworthsDataProviderTest {

    private static final Logger LOG = Logger.getLogger(WoolworthsDataProviderTest.class);

    @PersistenceContext
    private EntityManager entityManager;

    @TestHTTPResource("WoolworthsTestData2.json")
    URL wwTestData2;

    @TestHTTPResource("WoolworthsTestData.json")
    URL wwSaleTestData;

    @TestHTTPResource("WoolworthsAddressTestData.json")
    URL wwAddressData;

    @Inject
    ObjectMapper objMapper;

    @Inject
    WoolworthsPageItemProxy proxy;

    @Inject
    ProductRepo productRepo;

    @Inject
    StoreRepo storeRepo;

    @Inject
    WoolworthsDataMapper dataMapper;

    static Playwright playwright;
    static Browser browser;
    static BrowserContext bCtx;
    static Page page;

    @BeforeAll
    static void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                                                   .setProxy(new Proxy("http://localhost:8080")));
        bCtx = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
        page = bCtx.newPage();
    }

    @AfterAll
    static void destroy() {
        if (bCtx != null) {
            bCtx.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    void testJSONDeserialize() throws IOException {
        try (InputStream inputStream = wwTestData2.openStream()) {
            var searchResult = objMapper.readValue(inputStream, WoolworthsProductSearchResult.class);
            assert (searchResult != null);
            var resultItems = searchResult.getProducts().getItems().stream().filter(p -> p.getType().equals("Product")).toList();
            assertEquals(51, resultItems.size());
            assertNull(resultItems.getFirst().getMultibuy());
            var meatWithMultiBuy = resultItems.stream().filter(p -> "9421903748621".equals(p.getBarcode())).findFirst();
            meatWithMultiBuy.ifPresent(m -> assertNotNull(m.getMultibuy()));
        }
    }

    @Test
    void testScrape() {
        var allFetched = new CompletableFuture<List<WoolworthsProductQuote>>();
        List<WoolworthsProductQuote> collected = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger expectedTotal = new AtomicInteger(-1);

        page.route("https://www.woolworths.co.nz/api/v1/product**", route -> {
            APIResponse response = route.fetch();

            try {
                if (response.status() != 200) {
                    fail("Non-200 response: " + response.status() + " " + response.text());
                }
                var searchResult = objMapper.readValue(response.body(), WoolworthsProductSearchResult.class);
                // Filter non-product items (ads)
                var items = searchResult.getProducts().getItems().stream()
                                .filter(pq -> pq.getType().equals("Product")).toList();
                if (expectedTotal.get() < 0) {
                    expectedTotal.set(searchResult.getProducts().getTotalItems());
                    LOG.infov("Total items to fetch: {0}", expectedTotal.get());
                }

                collected.addAll(items);
                items.forEach(item -> LOG.infov("{0}:[{1}] - ${2,number,#.##}", item.getName(), item.getBarcode(), item.getPrice().getOriginalPrice()));

                if (expectedTotal.get() >= 0 && collected.size() >= expectedTotal.get() && !allFetched.isDone()) {
                    allFetched.complete(new ArrayList<>(collected));
                }

                route.fulfill(new Route.FulfillOptions().setResponse(response));
            } catch (IOException e) {
                fail("Failed to deserialize JSON response: " + response.text(), e);
            }
        });
        page.navigate("https://www.woolworths.co.nz/shop/searchproducts?search=cheese");

        Locator nextPage = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Next"));
        int safety = 100;
        while (!allFetched.isDone() && safety-- > 0) {
            // If no total known yet, wait a short moment for the first API response to arrive
            if (expectedTotal.get() < 0) {
                page.waitForTimeout(150);
                continue;
            }
            if (!nextPage.isEnabled()) break;
            nextPage.click();
            // small pause to allow request/response to complete before next click
            page.waitForTimeout(200);
        }

        // Wait for completion (or fail if not completed in reasonable time)
        var finalList = allFetched.join();
    }

    @Test
    void testFetchProductNoTransform() {
        Multi<WoolworthsProductQuote> m = proxy.fetchProductQuoteWithoutTransform("cheese");
        m.subscribe().asStream().forEach(item -> LOG.infov("{0}:[{1}] - ${2,number,#.##}", item.getName(), item.getBarcode(), item.getPrice().getOriginalPrice()));
    }

    @Test
    @Transactional
    void testDataConversion() throws IOException {
        try (InputStream inputStream = wwSaleTestData.openStream()) {
            var searchResult = objMapper.readValue(inputStream, WoolworthsProductSearchResult.class);
            assert (searchResult != null);
            var resultItems = searchResult.getProducts().getItems();
            assertEquals(3, resultItems.size());
            assertThat(resultItems, everyItem(notNullValue()));
            resultItems.forEach(pq -> LOG.infov("name: {0}; price: {1}", pq.getName(), pq.getPrice().getOriginalPrice()));
            LOG.infov("Datamapper runtime: {0}", dataMapper.getClass().getName());
            List<Quote> transformedList = resultItems.stream().map(dataMapper::toQuote).toList();
            Store s = storeRepo.findById(1L).orElseThrow();
            transformedList.forEach(q -> q.setQuoteStore(s));
            var chickenBreast = transformedList.stream().findFirst();
            chickenBreast.ifPresentOrElse(c -> {
                    assertEquals("woolworths nz chicken breast skinless boneless large tray min order 1kg", c.getProduct().getName());
                    assertNotNull(c.getDiscount());
                    assertEquals("14.99", c.getUnitPrice().toPlainString());
                    assertEquals(Product.Unit.PER_KG, c.getProduct().getUnit());
                    assertEquals("13.6", c.getDiscountedUnitPrice().toPlainString());
                    assertEquals("1.39", c.getDiscount().getSaveValue().toPlainString());
                },
                () -> Assertions.fail("Chicken breast not present"));

            var meatWithMultiBuy = transformedList.stream().filter(q -> q.getProduct().getGtin().equals("9400597064514")).findFirst();
            meatWithMultiBuy.ifPresentOrElse(m -> {
                    assertNotNull(m.getDiscount());
                    assertEquals(3, m.getDiscount().getMultiBuyQuantity());
                    assertEquals(20, m.getDiscount().getSaveValue().intValue());
                    assertEquals(new BigDecimal("6.67"), m.getDiscount().getSalePrice());
                    entityManager.persist(m);
                    GroupProductCode gpc = new GroupProductCode();
                    gpc.setProduct(m.getProduct());
                    gpc.setStoreGroup(s.getGroup());
                    entityManager.persist(gpc);
                },
                () -> Assertions.fail("Multibuy meat not present"));

            var drumstick = transformedList.stream().filter(q -> q.getProduct().getGtin().equals("9400597055772")).findFirst();
            drumstick.ifPresentOrElse(d -> {
                    assertEquals("woolworths nz chicken drumsticks tray 0.9-1.5kg 7-11pc", d.getProduct().getName());
                    assertEquals("8.0", d.getPrice().toPlainString());
                    //assertEquals();
                    GroupProductCode gpc = new GroupProductCode();
                    gpc.setProduct(d.getProduct());
                    gpc.setStoreGroup(s.getGroup());
                    entityManager.persist(gpc);
                },
                () -> Assertions.fail("Drumstick not present"));

            entityManager.persist(chickenBreast.orElseThrow());
            entityManager.persist(meatWithMultiBuy.orElseThrow());

            //productRepo.persist(chickenBreast.orElseThrow());
            //productRepo.persist(meatWithMultiBuy.orElseThrow());
        }
    }

    @Test
    @Disabled
    void testFetchandInsertToDB() {
        proxy.fetchProductQuote("cheese").subscribe();
    }

    @Test
    void testParseAddresses() throws IOException {
        try (InputStream inputStream = wwAddressData.openStream()) {
            var addressRsp = objMapper.readValue(inputStream, WoolworthsAddressResponse.class);
            List<WoolworthsStoreAddress> addresses = addressRsp.getStoreAreas().getStoreAddresses();
            assertThat(addresses, hasItem(allOf(
                hasProperty("id", is(1225718L)),
                hasProperty("name", is("Woolworths Northlands")),
                hasProperty("address", is("cnr Main North & Sawyers Arms Roads,Northlands Click and Collect,8051,Northlands Click and Collect"))
            )));
        }
    }

    @Test
    void testFetchStore() {
        Uni<List<Store>> storeListResult = proxy.getStoreList();
        storeListResult.subscribe().with(storeList -> {
            storeList.forEach(storeRepo::persist);
            proxy.setStore(storeList.getFirst());
        }, Assertions::fail);
        //storeListResult.chain(l -> proxy.setStore(l.getFirst())).subscribe().with(_ -> {}, Assertions::fail);
    }
}
