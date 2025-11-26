package io.equalink;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.equalink.pricekeep.service.dto.BaseEntity;
import io.equalink.pricekeep.service.dto.QuoteDTO;
import io.equalink.pricekeep.service.dto.StoreInfo;
import jakarta.data.Limit;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
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
}
