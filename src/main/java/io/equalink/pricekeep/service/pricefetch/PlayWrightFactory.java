package io.equalink.pricekeep.service.pricefetch;


import com.microsoft.playwright.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Path;
import java.util.List;

@ApplicationScoped
public class PlayWrightFactory {
    private Browser browser;
    private Playwright playwright;

    @PostConstruct
    protected void init() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();

    }

    @PreDestroy
    protected void cleanup() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Produces
    @Named("direct")
    public Page getBrowserPageInstance() {
        return browser.newPage();
    }

    @Produces
    public BrowserContext getBrowserContext() {
        return browser.newContext();
    }

    @ConfigProperty(name = "proxyAddr")
    String proxyAddr;

    @Produces
    @Named("withProxy")
    public Page getBrowserPageInstanceWithProxy() {
        //Log.infov("Creating new instance with proxy @ {0}", proxyAddr);
        //Proxy proxy = new Proxy(proxyAddr);
        //NewPageOptions options = new NewPageOptions().setProxy(proxy).setIgnoreHTTPSErrors(true);
        return browser.newPage();
    }

    @Produces
    @Named("withHead")
    public BrowserContext getHeadedModeBrowserInstance() {
        return playwright.chromium().launchPersistentContext(Path.of("./userProfile"),
            new BrowserType.LaunchPersistentContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .setViewportSize(1920, 1080)
                .setDeviceScaleFactor(1)
                .setHasTouch(false)
                .setLocale("en-NZ")
                .setTimezoneId("Pacific/Auckland")
                .setChannel("chrome")
                .setArgs(List.of("--disable-blink-features=AutomationControlled", // Removes 'navigator.webdriver'
                    "--disable-infobars",
                    "--no-sandbox"))

        );
    }

    void close(@Disposes Page page) {
        page.close();
    }

    void close(@Disposes BrowserContext ctx) {
        ctx.close();
    }
}
