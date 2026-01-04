package io.equalink.pricekeep.service.pricefetch.woolworths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.service.pricefetch.BaseScraper;
import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.Identifier;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.GeneratorEmitter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.SneakyThrows;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
@Named("woolworths")
@JBossLog
public class WoolworthsProductQuoteFetchService extends BaseScraper<WoolworthsProductQuote> {

    private static final String SEARCH_URL = "https://www.woolworths.co.nz/shop/searchproducts?search=";
    private static final String API_URL = "https://www.woolworths.co.nz/api/v1/product**";
    private static final String ASSET_URL = "https://assets.woolworths.com.au/images/**";
    private static final String CHANGE_ADDR_URL = "https://www.woolworths.co.nz/bookatimeslot/";
    private static final String GET_STORE_URL = "https://www.woolworths.co.nz/api/v1/addresses/pickup-addresses";
    private static final String SET_STORE_URL = "https://www.woolworths.co.nz/api/v1/fulfilment/my/pickup-addresses";
    private static final String IMAGE_FILTER = "**/*.{png,jpg,jpeg,gif,svg,webp}";
    private static final Logger LOG = Logger.getLogger(WoolworthsProductQuoteFetchService.class);

    private static final int ITEM_PER_PAGE = 120;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Inject
    ObjectMapper objMapper;

    @Inject
    @Named("withHead")
    BrowserContext browserContext;

    Page page;

    @Inject
    WoolworthsDataMapper dataMapper;

    private final AtomicReference<Locator> nextPageRef = new AtomicReference<>();
    private final List<WoolworthsProductQuote> collected = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, String> imageMapping = Collections.synchronizedMap(new HashMap<>());

    @PostConstruct
    public void init() {
        this.page = browserContext.newPage();
    }

    @PreDestroy
    public void destroy() {
        page.close();
    }

    @SneakyThrows
    private void handleRoute(Route route) {
        String newReqURL = route.request().url().replace("size=48", "size=" + ITEM_PER_PAGE);
        APIResponse response = route.fetch(new Route.FetchOptions().setUrl(newReqURL));
        LOG.info("Capturing content");
        if (response.status() != 200) {
            throw new RuntimeException("Non-200 response: " + response.status() + " " + response.text());
        }
        var searchResult = objMapper.readValue(response.body(), WoolworthsProductSearchResult.class);
        // Filter non-product items (ads)
        var items = searchResult.getProducts().getItems().stream()
                        .filter(pq -> pq.getType().equals("Product")).toList();

        // Collect images for items
        items.forEach(p -> {
            imageMapping.put(p.getSku(), p.getBarcode());
            LOG.debugv("Product SKU {0} is associated with product barcode {1}", p.getSku(), p.getBarcode());
        });

        expectedTotal = searchResult.getProducts().getTotalItems();
        LOG.infov("Total items to fetch: {0}", expectedTotal);

        collected.addAll(items);

        route.fulfill(new Route.FulfillOptions().setResponse(response));
    }

    @Override
    protected void initSearch(String keyword, GeneratorEmitter<? super List<WoolworthsProductQuote>> em) {
        page.unrouteAll();
        page.route(API_URL, this::handleRoute);

        page.route(ASSET_URL, route -> {
            if (route.request().resourceType().equals("image")) {
                String reqUrl = route.request().url();
                APIResponse response = route.fetch();
                String contentType = response.headers().get("content-type");
                if (contentType == null) {
                    log.warnv("Content-Type is null for image {0}", reqUrl);
                    response.headers().forEach((k, v) -> log.warnv("{0}: {1}", k, v));
                    route.fulfill();
                    return;
                }
                String skuFromFileName = reqUrl.substring(reqUrl.lastIndexOf("/") + 1, reqUrl.lastIndexOf("."));
                String fileExt = contentType.substring(contentType.indexOf("/") + 1);
                log.infov("SKU: {0}, FileExt: {1}", skuFromFileName, fileExt);
                String gtin = imageMapping.getOrDefault(skuFromFileName, skuFromFileName);
                imageMapping.put(gtin, fileExt);
                try {
                    super.writeToPath(gtin + "." + fileExt, response.body());
                } catch (IOException e) {
                    em.fail(e);
                }
                route.fulfill(new Route.FulfillOptions().setResponse(response));
            }
        });

        page.navigate(SEARCH_URL + keyword);
        nextPageRef.set(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Next")));
        page.waitForTimeout(300);
    }

    @Override
    protected Quote mapperFunction(WoolworthsProductQuote item) {
        Quote q = dataMapper.toQuote(item);
        if (q.getProduct() != null) {
            q.getProduct().setImgPath(String.format("/static/assets/img/%s.%s", q.getProduct().getGtin(), imageMapping.get(q.getProduct().getGtin())));
        }
        return q;
    }

    @Override
    protected List<WoolworthsProductQuote> fetchNext() throws RuntimeException {
        LOG.infov("Entering fetchNext()");
        LOG.infov("Collected array size: {0}", collected.size());
        if (collected.isEmpty()) {
            Locator nextPage = nextPageRef.get();
            LOG.info("Loading next page");
            nextPage.click();
            page.waitForTimeout(300);
        }
        List<WoolworthsProductQuote> result = new ArrayList<>(collected);
        collected.clear();
        return result;
    }

    @Override
    public Multi<Store> fetchStore() {
        return Multi.createFrom().emitter(emitter -> {
            page.unrouteAll();
            page.route(IMAGE_FILTER, Route::abort);
            page.route(GET_STORE_URL, route -> {
                try {
                    WoolworthsAddressResponse rsp = objMapper.readValue(route.fetch().body(), WoolworthsAddressResponse.class);
                    var storeList = rsp.getStoreAreas().getStoreAddresses();
                    LOG.infov("Getting results from {0}, count {1}", GET_STORE_URL, storeList.size());

                    storeList.stream()
                        .map(dataMapper::toStore)
                        .filter(addr -> addr.getName().startsWith("Woolworths"))
                            .forEach(emitter::emit);

                    emitter.complete();
                } catch (IOException e) {
                    emitter.fail(e);
                }
            });
            LOG.info("Visiting base URL");
            page.navigate(CHANGE_ADDR_URL);
            page.screenshot(new Page.ScreenshotOptions()
                                .setPath(Paths.get("D:\\screenshot-" + LocalDateTime.now().format(FORMATTER)+".png"))
                                .setFullPage(true));
            page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("Pick up")).check();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Change store")).click();
            LOG.info("Triggering getStore API call now");
        });
    }

    @Override
    public Uni<Void> setStore(String storeInternalCode) {
        return Uni.createFrom().emitter(emitter -> {
            page.unrouteAll();
            page.route(IMAGE_FILTER, Route::abort);
            page.route(SET_STORE_URL, route -> {
                LOG.infov("Postdata: {0}", route.request().postData());
                String newPostBody = String.format("{\"addressId\": %s}", storeInternalCode);
                LOG.infov("Replace with: {0}", newPostBody);
                APIResponse rsp = route.fetch(new Route.FetchOptions().setPostData(newPostBody).setMethod("put"));
                //LOG.infov("Response code: {0}, response body {1}", rsp.status(), rsp.text());
                if (rsp.ok())
                    emitter.complete(null);
                else
                    emitter.fail(new RuntimeException("Return error: " + rsp.status()));
            });

            page.navigate(CHANGE_ADDR_URL);
            page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("Pick up")).check();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Change store")).click();
            page.getByLabel("Region").selectOption("494");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Woolworths Alexandra 106")).click();
        });


    }
}
