package io.equalink.pricekeep.service.pricefetch.woolworths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.service.pricefetch.BaseScraper;
import io.smallrye.common.annotation.Identifier;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class WoolworthsPageItemProxy extends BaseScraper<WoolworthsProductQuote> {

    private static final String SEARCH_URL = "https://www.woolworths.co.nz/shop/searchproducts?search=";
    private static final String API_URL = "https://www.woolworths.co.nz/api/v1/product**";
    private static final String ASSET_URL = "https://assets.woolworths.com.au/images/**";
    private static final String CHANGE_ADDR_URL = "https://www.woolworths.co.nz/bookatimeslot/";
    private static final String GET_STORE_URL = "https://www.woolworths.co.nz/api/v1/addresses/pickup-addresses";
    private static final String SET_STORE_URL = "https://www.woolworths.co.nz/api/v1/fulfilment/my/pickup-addresses";
    private static final String IMAGE_FILTER = "**/*.{png,jpg,jpeg,gif,svg,webp}";
    private static final Logger LOG = Logger.getLogger(WoolworthsPageItemProxy.class);

    private static final int ITEM_PER_PAGE = 120;

    @Inject
    ObjectMapper objMapper;

    @Inject
    @Identifier("withProxy")
    Page page;

    @Inject
    WoolworthsDataMapper dataMapper;

    private final AtomicReference<Locator> nextPageRef = new AtomicReference<>();
    private final List<WoolworthsProductQuote> collected = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, String> imageMapping = Collections.synchronizedMap(new HashMap<>());

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
            imageMapping.put(p.getImages().get("big"), p.getBarcode());
            LOG.infov("Putting {0}:{1}", p.getImages().get("big"), p.getBarcode());
        });

        expectedTotal = searchResult.getProducts().getTotalItems();
        LOG.infov("Total items to fetch: {0}", expectedTotal);

        collected.addAll(items);

        route.fulfill(new Route.FulfillOptions().setResponse(response));
    }

    @Override
    protected void initSearch(String keyword) {
        page.unrouteAll();
        page.route(API_URL, this::handleRoute);

        page.route(ASSET_URL, route -> {
            if (route.request().resourceType().equals("image")) {
                LOG.infov("Path: {0}", route.request().url());
            }
            if (route.request().resourceType().equals("image") && imageMapping.containsKey(route.request().url())) {
                APIResponse response = route.fetch();
                String gtin = imageMapping.get(route.request().url());
                LOG.infov("Writing image to {0}", super.imgPath);
                super.writeToPath(gtin + ".jpg", response.body());
                route.fulfill();
            }
        });

        page.navigate(SEARCH_URL + keyword);
        nextPageRef.set(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Next")));
        page.waitForTimeout(300);
    }

    @Override
    protected Quote mapperFunction(WoolworthsProductQuote item) {
        return dataMapper.toQuote(item);
    }

    @Override
    protected List<WoolworthsProductQuote> fetchItem() throws RuntimeException {
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
    public Uni<List<Store>> getStoreList() {
        return Uni.createFrom().emitter( emitter -> {
            page.unrouteAll();
            page.route(IMAGE_FILTER, Route::abort);
            page.route(GET_STORE_URL, route -> {
                try {
                    WoolworthsAddressResponse rsp = objMapper.readValue(route.fetch().body(), WoolworthsAddressResponse.class);
                    var storeList = rsp.getStoreAreas().getStoreAddresses();
                    LOG.infov("Getting results from {0}, count {1}", GET_STORE_URL, storeList.size());

                    emitter.complete(storeList.stream()
                                        .map(dataMapper::toStore)
                                        .filter(addr -> addr.getName().startsWith("Woolworths"))
                                        .toList());
                } catch (IOException e) {
                    emitter.fail(e);
                }
            });
            LOG.info("Visiting base URL");
            page.navigate(CHANGE_ADDR_URL);
            page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("Pick up")).check();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Change store")).click();
            LOG.info("Triggering API call now");
        });
    }

    @Override
    public Uni<Void> setStore(Store store) {
        return Uni.createFrom().emitter(emitter -> {
            page.unrouteAll();
            page.route(IMAGE_FILTER, Route::abort);
            page.route(SET_STORE_URL, route -> {
                LOG.infov("Postdata: {0}", route.request().postData());
                String newPostBody = String.format("{\"addressId\": %s}", store.getInternalId());
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
