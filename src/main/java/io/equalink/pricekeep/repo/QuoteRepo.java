package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.service.dto.CompactQuote;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import org.hibernate.annotations.processing.SQL;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QuoteRepo {

    @Save
    void persist(Quote q);


    @SQL("""
        SELECT s.name as store_name, quote_date, price, dct_unit_price, quote_source, unit_price, discount_id, product_id, store_id
        FROM (
            SELECT *,
                   ROW_NUMBER() OVER (PARTITION BY quote_date, product_id ORDER BY price ASC) as rn
            FROM quote
        ) q
        left join store s on s.id = q.store_id
        WHERE q.rn = 1 and q.product_id = :productId and quote_date >= :cutoffDate
        """)
    List<CompactQuote> findLowestQuotePerDayHistByProduct(Long productId, LocalDate cutoffDate);
}