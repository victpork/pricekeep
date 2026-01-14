package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.data.Alert;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;

@Repository
public interface AlertRepo {

    @Save
    void createAlert(Alert alert);

    @Delete
    void removeAlert(Alert alert);

    @Query("where product.id = :productId")
    Alert getAlert(Long productId);
}
