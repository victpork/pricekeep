package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.service.dto.CompactQuote;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import org.hibernate.annotations.processing.SQL;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepo {

    @Save
    void persist(Quote q);

    @Query("from Quote q where q.product.id = :productId and q.quoteStore.id = :storeId and q.quoteDate = :quoteDate")
    Optional<Quote> findById(Long productId, Long storeId, LocalDate quoteDate);


    default void persistIfNotExists(Quote q) {
        if (findById(q.getProduct().getId(), q.getQuoteStore().getId(), q.getQuoteDate()).isEmpty()) {
            persist(q);
        }
    }


    @SQL("""
        SELECT s.name as store_name, quote_date, price, dct_unit_price, quote_source, unit_price, discount_id, product_id, store_id
        FROM (
            SELECT *,
                   ROW_NUMBER() OVER (PARTITION BY quote_date, product_id ORDER BY price ASC) as rn
            FROM quote
        ) q
        left join store s on s.id = q.store_id
        WHERE q.rn = 1 and q.product_id = :productId and quote_date >= :cutoffDate order by quote_date desc
        """)
    List<CompactQuote> findLowestQuotePerDayHistByProduct(Long productId, LocalDate cutoffDate);
}