package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.data.Batch;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;

import java.util.List;

@Repository
public interface BatchRepo {

    @Find
    List<Batch> findAll();

    @Find
    Batch findById(Long id);

    @Save
    void persist(Batch batch);

    @Delete
    void delete(Batch batch);
}
