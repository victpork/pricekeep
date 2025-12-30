package io.equalink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.MouseButton;
import io.equalink.pricekeep.service.pricefetch.paknsave.PaknSaveProductSearchResult;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@QuarkusTest
@JBossLog
public class PaknSaveProductQuoteFetchServiceTest {
    static Random rand = new Random();

    @Inject
    ObjectMapper objMapper;

    @Test
    void testPnSGetData() {
        Playwright playwright = Playwright.create();
        BrowserContext browserContext = playwright.chromium().launchPersistentContext(Path.of("./userProfile"),
            new BrowserType.LaunchPersistentContextOptions()
                .setGeolocation(-36.8604, 174.6296)
                .setPermissions(List.of("geolocation"))
                .setHeadless(false)
                .setChannel("chrome")
                .setViewportSize(null)
        );
        Page page = browserContext.newPage();
        browserContext.route("https://api-prod.paknsave.co.nz/v1/edge/store/**/product/**", route -> {
            APIResponse rsp = route.fetch();
            log.infov("Per product page rsp: {0}", rsp.text());
        });
        CompletableFuture<PaknSaveProductSearchResult> res = new CompletableFuture<>();
        page.route("**/products", route -> {
            APIResponse rsp = route.fetch();
            try {
                PaknSaveProductSearchResult result = objMapper.readValue(rsp.body(), PaknSaveProductSearchResult.class);
                log.infov("Result count: {0}, number of pages: {1}", result.getTotalHits(), result.getTotalPages());
                result.getProducts().forEach(System.out::println);
                res.complete(result);
            } catch (IOException e) {
                res.completeExceptionally(e);
            }
            route.fulfill();
        });
        page.navigate("https://www.paknsave.co.nz/shop/category/fridge-deli-and-eggs/cheese?pg=1");
        page.waitForTimeout(rndHumanReactionTime());
        if (page.title().equals("Just a moment...")) {
            page.waitForTimeout(10000);
        }
        Locator humanCheckbox = page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Verify you are human"));
        if (humanCheckbox.isVisible()) {
            humanCheckbox.click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
        }
        //assertEquals("Cheese | PAK'nSAVE Online", page.title());
        try {
            PaknSaveProductSearchResult searchResult = res.get();
            List<String> productNames = searchResult.getProducts().stream().map(p -> p.getBrand() + " " + p.getName() + " " + p.getDisplayName()).toList();
            productNames.forEach(name -> {
                Page newPage = browserContext.waitForPage(() -> {
                    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name)).click(new Locator.ClickOptions()
                                                       .setButton(MouseButton.MIDDLE));
                });
                newPage.waitForLoadState(LoadState.NETWORKIDLE);
                if (page.title().equals("Just a moment...")) {
                    page.waitForTimeout(10000);
                }
                page.waitForTimeout(rndHumanReactionTime());
                newPage.close();
            });

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            browserContext.close();
            playwright.close();
        }
        //log.info(rspText);
        //Try to loop the links



    }

    static Long rndHumanReactionTime() {
        return rand.nextLong(300L, 1500L);
    }
}
