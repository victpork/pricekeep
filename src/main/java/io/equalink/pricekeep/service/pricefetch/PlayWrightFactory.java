package io.equalink.pricekeep.service.pricefetch;



import io.quarkus.logging.Log;
import jakarta.annotation.PreDestroy;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Browser.NewPageOptions;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Proxy;

import io.smallrye.common.annotation.Identifier;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

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
    @RequestScoped
    Page getBrowserPageInstance() {
        return browser.newPage();
    }

    @Produces
    @RequestScoped
    BrowserContext getBrowserContext() {
        return browser.newContext();
    }

    @Produces
    @RequestScoped
    @Identifier("withProxy")
    Page getBrowserPageInstanceWithProxy(@ConfigProperty(name = "proxyAddr") String serverAddr) {
        Log.infov("Creating new instance with proxy @ {0}", serverAddr);
        Proxy proxy = new Proxy(serverAddr);
        NewPageOptions options = new NewPageOptions().setProxy(proxy).setIgnoreHTTPSErrors(true);
        return browser.newPage(options);
    }

    void close(@Disposes Page page) {
        page.close();
    }

    void close(@Disposes BrowserContext ctx) {
        ctx.close();
    }
}
