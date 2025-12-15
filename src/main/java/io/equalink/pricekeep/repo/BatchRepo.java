package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.data.BatchExecDetail;
import jakarta.data.repository.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepo {

    @Find
    List<BaseBatch> findAll();

    @Query("select id, name, jobType, cronTrigger from BaseBatch")
    List<BatchExecDetail> getAllBatchesForExecution();

    @Find
    Optional<BaseBatch> findById(Long id);

    @Find
    Optional<BaseBatch> findByName(String name);

    @Save
    void persist(BaseBatch batch);

    @Delete
    void delete(BaseBatch batch);
}
