package io.equalink.pricekeep.service.pricefetch;


import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.GeneratorEmitter;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@JBossLog
public abstract class BaseScraper<T> implements ProductQuoteFetchService, StoreFetchService {

    protected abstract void initSearch(String keyword, GeneratorEmitter<? super List<T>> em);

    protected abstract Quote mapperFunction(T item);

    protected abstract List<T> fetchNext() throws IOException;

    protected int expectedTotal;

    public abstract Uni<Void> setStore(String storeInternalCode);

    public abstract Multi<Store> fetchStore();

    @ConfigProperty(name = "fetchImgPath")
    protected Path imgPath;

    public Multi<Quote> fetchProductQuote(Store store, String keyword) {
        return this.fetchProductQuoteWithoutTransform(store.getInternalId(), keyword)
                   .onItem().<T>disjoint()
                   .onItem().transform(item -> {
                Quote q = mapperFunction(item);
                q.setQuoteStore(store);
                return q;
            });
    }


    public Multi<List<T>> fetchProductQuoteWithoutTransform(String storeInternalCode, String keyword) {
        return Multi.createFrom().generator(() -> 0, (prevCnt, em) -> {
            if (prevCnt == 0) {
                log.infov("Initialising routes setup");
                if (storeInternalCode != null) {
                    setStore(storeInternalCode).await().atMost(Duration.ofMinutes(1));
                }
                initSearch(keyword, em);
            }
            log.infov("Running fetchHelper with {0} prev records", prevCnt);
            int res = fetchHelper(prevCnt, em);
            log.infov("Going to next stage with statenumber: {0}", res);
            return res;
        });
    }

    protected int fetchHelper(int prevFetchCnt, GeneratorEmitter<? super List<T>> emitter) {
        try {
            List<T> lastRes = fetchNext();
            emitter.emit(lastRes);
            log.infov("Current fetch size: {0}; accumulate size: {1}; expected total: {2}", lastRes.size(), prevFetchCnt + lastRes.size(), this.expectedTotal);
            if (prevFetchCnt + lastRes.size() >= expectedTotal || lastRes.isEmpty()) {
                log.infov("Completed loading with size {0}", prevFetchCnt + lastRes.size());
                emitter.complete();
            }
            return prevFetchCnt + lastRes.size();
        } catch (IOException e) {
            emitter.fail(e);
        }

        return prevFetchCnt;
    }

    protected void writeToPath(String fileName, byte[] fileContent) throws IOException {
        if (imgPath == null) {
            log.warn("`imgPath` is not configured; skipping write for: " + fileName);
            throw new IOException("imgPath empty");
        }
        if (fileName == null || fileName.trim().isEmpty() || fileContent == null) {
            log.warn("Invalid fileName or fileContent; skipping write");
            throw new IOException("fileName empty or content is null");
        }
        Files.createDirectories(imgPath);
        Path target = imgPath.resolve(fileName);
        Files.write(target, fileContent);
        log.debugf("Wrote file to `%s`", target.toString());

    }
}
