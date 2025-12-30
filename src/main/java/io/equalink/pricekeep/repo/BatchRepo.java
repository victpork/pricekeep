package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.data.ProductQuoteImportBatch;
import io.equalink.pricekeep.data.StoreImportBatch;
import jakarta.data.repository.*;
import jakarta.transaction.Transactional;
import org.hibernate.StatelessSession;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepo {

    StatelessSession session();

    @Find
    List<BaseBatch> findAll();

    @Find
    List<BaseBatch> getAllBatchesForExecution();

    @Query("from ProductQuoteImportBatch pqb left join fetch pqb.source where pqb.id = :id")
    Optional<ProductQuoteImportBatch> findPQBatchById(Long id);

    @Query("from StoreImportBatch sib left join fetch sib.storeGroup where sib.id = :id")
    Optional<StoreImportBatch> findStoreBatchById(Long id);


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
