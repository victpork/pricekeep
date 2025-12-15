package io.equalink;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.equalink.pricekeep.service.dto.BaseEntity;
import io.equalink.pricekeep.service.dto.QuoteDTO;
import io.equalink.pricekeep.service.dto.StoreInfo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.groups.GeneratorEmitter;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import jakarta.data.Limit;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Test;

import io.equalink.pricekeep.data.Product;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.repo.ProductRepo;
import io.equalink.pricekeep.repo.QuoteRepo;
import io.equalink.pricekeep.service.dto.QuoteMapper;
import io.equalink.pricekeep.service.quote.ProductService;
import io.equalink.pricekeep.service.quote.QuoteService;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;

@QuarkusTest
@JBossLog
public class EntityTest {

    @Inject
    QuoteMapper qm;

    @Inject
    QuoteService qs;

    @Inject
    QuoteRepo qr;

    @Inject
    ProductService ps;

    @Inject
    ProductRepo pr;

    @Inject
    ObjectMapper om;

    @Test
    void testGetQuoteData() {
        Optional<Product> pd = pr.findByGTIN(
            "1234567890123"
        );
        assertTrue(pd.isPresent());
        pd.ifPresent(p -> assertEquals("Milk", p.getName()));
    }

    @Test
    @TestTransaction
    void testPersistQuote() {
        Store s = new Store();
        s.setId(1L);
        Product p = Product.builder()
                        .id(1L)
                        .build();

        Quote q = Quote.builder()
                      .product(p)
                      .quoteStore(s)
                      .quoteDate(LocalDate.now())
                      .price(new BigDecimal(7))
                      .quoteSource(Quote.Source.USER)
                      .build();

        qr.persist(q);
    }


    @Test
    void testGetProductListName() {
        List<String> pnList = ps.getProductListName();
        assertEquals(3, pnList.size());
        assertThat(pnList, containsInAnyOrder("Milk", "Cereal", "Bread"));
    }

    @Test
    void testSearch() {
        List<Product> products = pr.find("cer%", new Sort<>("name", true, true), PageRequest.ofPage(1));
        assertEquals(1, products.size());
        Product cereal = products.getFirst();
        var cerealQuotes = cereal.getPriceQuotes();
        assertEquals(5, cerealQuotes.size());
        assertThat(cerealQuotes, hasItems(
                allOf(
                    hasProperty("quoteDate", is(LocalDate.of(2025, 3, 10))),
                    hasProperty("price", is(new BigDecimal("7.50")))
                ),
                allOf(
                    hasProperty("quoteDate", is(LocalDate.of(2025, 3, 9))),
                    hasProperty("price", is(new BigDecimal("7.29")))
                ),
                allOf(
                    hasProperty("quoteDate", is(LocalDate.of(2025, 3, 9))),
                    hasProperty("price", is(new BigDecimal("5.59")))
                )
        ));
    }

    @Test
    void multiTest() {
        Multi<String> multi = Multi.createFrom().generator(() -> 0, (i, em) -> {
            if (i > 5) {
                em.complete();
            } else {
                em.emit(List.of("i=" + i, "i=" + i));
            }

            return i+1;
        }).onItem().<String>disjoint().onItem().transform(i -> i);
        AssertSubscriber<String> subscriber = multi.subscribe().withSubscriber(AssertSubscriber.create(10));
        subscriber.assertCompleted().assertItems("i=0","i=0","i=1","i=1","i=2","i=2","i=3","i=3");
    }

    @Test
    void multiTestWithoutAssertComplete() {
        Multi<String> multi = Multi.createFrom().generator(() -> 0, (i, em) -> {
            if (i > 5) {
                em.complete();
            } else {
                em.emit(List.of("i=" + i, "i=" + i));
            }

            return i+1;
        }).onItem().disjoint();
        CompletableFuture<Void> syncFlag = new CompletableFuture<>();
        List<String> results = new ArrayList<>();
        multi.subscribe().with(results::add, () -> syncFlag.complete(null));
        syncFlag.join();
        assertThat(results, hasSize(12));
        assertThat(results, hasItems("i=0","i=0","i=1","i=1","i=2","i=2","i=3","i=3"));
    }

    @Test
    void testMultiSample() {
        Multi<Integer> multi = Multi.createFrom().range(1, 5)
                                   .onItem().transform(n -> n * 10);

        AssertSubscriber<Integer> subscriber = multi.subscribe().withSubscriber(AssertSubscriber.create(10));

        subscriber.assertCompleted()
            .assertItems(10, 20, 30, 40);
    }
}
