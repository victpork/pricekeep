package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.data.BaseBatch;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;

import java.util.List;

@Repository
public interface BatchRepo {

    @Find
    List<BaseBatch> findAll();

    @Find
    BaseBatch findById(Long id);

    @Save
    void persist(BaseBatch batch);

    @Delete
    void delete(BaseBatch batch);
}
