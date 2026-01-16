package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.data.Alert;
import io.equalink.pricekeep.data.Product;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.transaction.Transactional;
import org.hibernate.annotations.processing.SQL;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AlertRepo {

    @Transactional
    @Query("""
    insert into Alert(product, targetPrice) values(:product, :priceLevel) on conflict(product) 
    do update set targetPrice = :priceLevel
    """)
    void createAlert(Product product, BigDecimal priceLevel);

    @Delete
    void removeAlert(Alert alert);

    @Query("where product.id = :productId")
    Optional<Alert> getAlert(Long productId);
}
