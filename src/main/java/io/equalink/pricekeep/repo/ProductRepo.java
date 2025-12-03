package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.data.Product;
import io.equalink.pricekeep.data.Quote;
import jakarta.annotation.Nullable;
import jakarta.data.Limit;
import jakarta.data.Sort;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.*;
import jakarta.transaction.Transactional;
import org.hibernate.StatelessSession;
import org.hibernate.annotations.processing.Pattern;
import org.hibernate.annotations.processing.SQL;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepo {

    StatelessSession session();

    @Query("where :tags in tags")
    List<Product> findByTags(Set<String> tags);

    @Query("from Product p left join GroupProductCode gpc on p = gpc.product where p.gtin = :gtin or gpc.internalCode = :sku and gpc.storeGroup.name = :storeGroupCode")
    Optional<Product> findBySKUOrGTIN(String gtin, String sku, String storeGroupCode);

    @Find
    Optional<Product> findByGTIN(String gtin);

    @Find
    Optional<Product> findById(Long id);

    @Find
    List<Product> findByExactName(String name);

    @Query("select p from Product p left join fetch p.priceQuotes q " +
                   "where q.id in (select q2.id from Quote q2 where q2.product.id = p.id and " +
                   "(q2.quoteStore, q2.quoteDate) in (select q3.quoteStore, max(q3.quoteDate) from Quote q3 where q3.product.id = p.id group by q3.quoteStore)) and " +
                   "p.id = :id")
    Optional<Product> findByIdWithLatestQuotes(Long id);

    @Query("where lower(:keyword) member of tags or lower(name) like lower(concat('%', :keyword, '%'))")
    List<Product> findByKeyword(String keyword);

    @Query("""
        select p from Product p
         left join fetch p.priceQuotes q
         left join fetch q.quoteStore s
         left join fetch q.discount qd
         left join fetch p.priceStats ps
         where p.name ilike :name and
         q.id in (select q2.id from Quote q2 where q2.product.id = p.id
         and (q2.quoteStore, q2.quoteDate) in (select q3.quoteStore, max(q3.quoteDate) from Quote q3 where q3.product.id = p.id group by q3.quoteStore))
        """)
    List<Product> find(@Pattern String name, Sort<Product> sort, PageRequest pageRequest);

    @Query("""
        select p from Product p
         left join fetch p.priceQuotes q
         left join fetch q.quoteStore s
         left join fetch q.discount qd
         left join fetch p.priceStats ps
         where q.id is null or q.id in (select q2.id from Quote q2 where q2.product.id = p.id
         and (q2.quoteStore, q2.quoteDate) in (select q3.quoteStore, max(q3.quoteDate) from Quote q3 where q3.product.id = p.id group by q3.quoteStore))
        """)
    List<Product> findAll(Sort<Product> sort, PageRequest pageRequest);

    @SQL("""
        select distinct e from (
            select p.name as e from Product p
            union
            select distinct unnest(p.tags) as e from Product p
        )
    """)
    List<String> getKeywordList();

    @Save
    void persist(Product p);


    @Transactional
    default void persist(Quote q) {
        if (q.getProduct() != null && q.getProduct().getId() == null) {
            session().insert(q.getProduct());
            if (q.getProduct().getGroupSKU() != null && !q.getProduct().getGroupSKU().isEmpty()) {
                q.getProduct().getGroupSKU().forEach(k -> session().insert(k));
            }
        }
        session().insert(q);
        if (q.getDiscount() != null) {
            q.getDiscount().setQuote(q);
            session().insert(q.getDiscount());
        }
    }

    @Query("select q from Product p join p.priceQuotes q where p.id = :productId and q.quoteDate = (select max(q2.quoteDate) from p.Quote q2)")
    Optional<Quote> getLatestQuote(Long productId);

    @Query("select q from Product p join p.priceQuotes q where p.id = :productId and q.quoteDate >= :cutoff order by q.price asc")
    Optional<Quote> getLowestPriceSince(Long productId, LocalDate cutoff, Limit l);

    @SQL("""
            with
            mth_stat as (
                select
                    min(q.price) as min_price,
                    avg(q.price) as avg_price
                from Quote q
                where q.product_id = :pid and q.quote_date >= current_date - interval '1 month'
            ),
            quarter_stat as (
                select
                    min(q.price) as min_price,
                    avg(q.price) as avg_price
                from Quote q
                where q.product_id = :pid and q.quote_date >= current_date - interval '3 months'
            ),
            year_stat as (
                select
                    min(q.price) as min_price,
                    avg(q.price) as avg_price
                from Quote q
                where q.product_id = :pid and q.quote_date >= current_date - interval '1 year'
            ),
            update product_stats set month_min = mth_stat.min_price, month_avg = mth_stat.avg_price,
                quarter_min = quarter_stat.min_price, quarter_avg = quarter_stat.avg_price,
                year_min = year_stat.min_price, year_avg = year_stat.avg_price
            where product_id = :pid
            """)
    void updateProductPriceStats(Long pid);

    @Query("""
        select q from Quote q
        where q.quoteDate >= :cutoffDate
          and q.price < (
              select VALUE(p.priceStats) from Product p
              where p.id = q.product.id and
              KEY(p.priceStats) = ProductStatType.MONTH_AVG
          )
        order by q.product.id, q.price asc
    """)
    Page<Quote> getLatestDeals(PageRequest pageRequest, LocalDate cutoffDate);

    @Query("select q from Product p join p.priceQuotes q where " +
                   "p.id = :productId and " +
                   "q.quoteDate >= :cutoffDate " +
                   "and (:includeDiscount = true or q.discount IS NULL) " +
                   "order by q.quoteDate desc")
    List<Quote> getPriceHistory(Long productId, LocalDate cutoffDate, boolean includeDiscount);


    @Query("select q from Alert a join Quote q where q.quoteDate >= :cutoffDate " +
               "and q.price <= a.targetPrice ")
    List<Quote> getTriggeredAlerts(LocalDate cutoffDate);
}
