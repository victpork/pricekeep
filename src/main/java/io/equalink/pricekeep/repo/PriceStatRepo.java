package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.data.ProductStatRecord;
import jakarta.data.repository.Repository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.hibernate.annotations.processing.SQL;

@Repository
@Transactional
public interface PriceStatRepo {
    @SQL(
        """
            SELECT product_id as productId,

            -- 1 month aggregations
            MIN(price) OVER (
                ORDER BY quotedate
                RANGE BETWEEN INTERVAL '1 month' PRECEDING AND CURRENT ROW
            ) AS MMIN,
            AVG(price) OVER (
                ORDER BY quotedate
                RANGE BETWEEN INTERVAL '1 month' PRECEDING AND CURRENT ROW
            ) AS MAVG,

            -- 3 month aggregations
            MIN(price) OVER (
                ORDER BY quotedate
                RANGE BETWEEN INTERVAL '3 months' PRECEDING AND CURRENT ROW
            ) AS QMIN,
            AVG(price) OVER (
                ORDER BY quotedate
                RANGE BETWEEN INTERVAL '3 months' PRECEDING AND CURRENT ROW
            ) AS QAVG,

            -- 1 year aggregations
            MIN(price) OVER (
                ORDER BY quotedate
                RANGE BETWEEN INTERVAL '1 year' PRECEDING AND CURRENT ROW
            ) AS YMIN,
            AVG(price) OVER (
                ORDER BY quotedate
                RANGE BETWEEN INTERVAL '1 year' PRECEDING AND CURRENT ROW
            ) AS YAVG

        FROM quote
        where product_id in :productIdList
        group by product_id
            """
    )
    List<ProductStatRecord> updateProductPriceStats(List<Long> productIdList);

    @SQL(
        """
        select avg(price), min(price), product_id from quote
        where product_id in :productIdList
            """
    )
    List<ProductStatRecord> selectProductPriceStats(List<Long> productIdList);
}
