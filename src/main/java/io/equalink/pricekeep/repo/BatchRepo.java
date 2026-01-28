package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.batch.JobStatus;
import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.data.ProductQuoteImportBatch;
import io.equalink.pricekeep.data.StoreImportBatch;
import jakarta.data.repository.*;
import jakarta.transaction.Transactional;
import org.hibernate.StatelessSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepo {

    StatelessSession session();

    @Query("from StoreImportBatch b left join fetch b.storeGroup sg")
    List<StoreImportBatch> findAllStoreImportBatch();

    @Query("from ProductQuoteImportBatch b left join fetch b.source s")
    List<ProductQuoteImportBatch> findAllProductQuoteImportBatch();

    default List<BaseBatch> findAll() {
        List<BaseBatch> baseBatchList = new ArrayList<>();
        baseBatchList.addAll(findAllStoreImportBatch());
        baseBatchList.addAll(findAllProductQuoteImportBatch());
        return baseBatchList;
    }

    @Find
    List<BaseBatch> getAllBatchesForExecution();

    @Query("from ProductQuoteImportBatch pqb left join fetch pqb.source where pqb.id = :id")
    Optional<ProductQuoteImportBatch> findPQBatchById(Long id);

    @Query("from StoreImportBatch sib left join fetch sib.storeGroup where sib.id = :id")
    Optional<StoreImportBatch> findStoreBatchById(Long id);

    @Query("update BaseBatch b set b.lastRunResult = :status, b.lastRunTime = :time where b = :batch")
    void setBatchStatus(BaseBatch batch, JobStatus status, LocalDateTime time);

    @Transactional
    default Optional<? extends BaseBatch> findById(Long id) {
        var pqBatch = findPQBatchById(id);
        var siBatch = findStoreBatchById(id);
        if (pqBatch.isPresent()) return pqBatch;
        return siBatch;
    }

    @Query("from ProductQuoteImportBatch pqb left join fetch pqb.source where pqb.name = :name")
    Optional<ProductQuoteImportBatch> findPQBatchByName(String name);

    @Query("from StoreImportBatch sib left join fetch sib.storeGroup where sib.name = :name")
    Optional<StoreImportBatch> findStoreBatchByName(String name);

    @Query("update BaseBatch b set b.enabled = :enable where b = :batch")
    void setEnabled(BaseBatch batch, boolean enable);

    @Transactional
    default Optional<? extends BaseBatch> findByName(String name) {
        var pqBatch = findPQBatchByName(name);
        var siBatch = findStoreBatchByName(name);
        if (pqBatch.isPresent()) return pqBatch;
        return siBatch;
    }

    @Save
    void persist(BaseBatch batch);

    @Delete
    void delete(BaseBatch batch);
}
