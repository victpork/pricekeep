package io.equalink.pricekeep.service.pricefetch;


import io.equalink.pricekeep.data.Product;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.GeneratorEmitter;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class BaseScraper<T> implements ProductQuoteFetchService {

    private static final Logger LOG = Logger.getLogger(BaseScraper.class);

    protected abstract void initSearch(String keyword);

    protected abstract Quote mapperFunction(T item);

    protected abstract List<T> fetchItem() throws IOException;

    protected int expectedTotal;

    public abstract Uni<Void> setStore(Store store);

    public abstract Uni<List<Store>> getStoreList();

    @ConfigProperty(name = "fetchImgPath")
    protected Path imgPath;

    @Override
    public Multi<Quote> fetchProductQuote(String keyword) {
        return Multi.createFrom().<Integer, List<T>>generator(() -> {
                initSearch(keyword);
                return 0;
            }, this::fetchHelper)
                   .onItem().<T>disjoint()
                   .onItem().transform(this::mapperFunction);
    }

    public Multi<T> fetchProductQuoteWithoutTransform(String keyword) {
        return Multi.createFrom().<Integer, List<T>>generator(() -> {
                initSearch(keyword);
                return 0;
            }, this::fetchHelper)
                   .onItem().disjoint();
    }

    protected int fetchHelper(int prevFetchCnt, GeneratorEmitter<? super List<T>> emitter) {
        try {
            List<T> lastRes = fetchItem();
            emitter.emit(lastRes);
            LOG.infov("Current fetch size: {0}; accumulate size: {1}; expected total: {2}", lastRes.size(), prevFetchCnt + lastRes.size(), this.expectedTotal);
            if (prevFetchCnt + lastRes.size() >= expectedTotal || lastRes.isEmpty()) {
                emitter.complete();
            }
            return prevFetchCnt + lastRes.size();
        } catch (IOException e) {
            emitter.fail(e);
        }

        return prevFetchCnt;
    }

    @SneakyThrows
    protected void writeToPath(String fileName, byte[] fileContent) {
        if (imgPath == null) {
            LOG.warn("`imgPath` is not configured; skipping write for: " + fileName);
            throw new IOException("imgPath empty");
        }
        if (fileName == null || fileName.trim().isEmpty() || fileContent == null) {
            LOG.warn("Invalid fileName or fileContent; skipping write");
            throw new IOException("fileName empty or content is null");
        }
        Files.createDirectories(imgPath);
        Path target = imgPath.resolve(fileName);
        Files.write(target, fileContent);
        LOG.debugf("Wrote file to `%s`", target.toString());

    }
}
