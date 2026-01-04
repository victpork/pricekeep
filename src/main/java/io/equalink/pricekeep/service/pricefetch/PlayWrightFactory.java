package io.equalink.pricekeep.service.pricefetch;



import com.microsoft.playwright.*;
import io.quarkus.logging.Log;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.microsoft.playwright.Browser.NewPageOptions;
import com.microsoft.playwright.options.Proxy;

import io.smallrye.common.annotation.Identifier;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

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
        Log.infov("Creating new instance with proxy @ {0}", proxyAddr);
        Proxy proxy = new Proxy(proxyAddr);
        NewPageOptions options = new NewPageOptions().setProxy(proxy).setIgnoreHTTPSErrors(true);
        return browser.newPage(options);
    }

    @Produces
    @Named("withHead")
    public BrowserContext getHeadedModeBrowserInstance() {
        return playwright.chromium().launchPersistentContext(Path.of("./userProfile"),
            new BrowserType.LaunchPersistentContextOptions()
                .setHeadless(false)
                .setChannel("chrome")
                .setViewportSize(null)
        );
    }

    void close(@Disposes Page page) {
        page.close();
    }

    void close(@Disposes BrowserContext ctx) {
        ctx.close();
    }
}
