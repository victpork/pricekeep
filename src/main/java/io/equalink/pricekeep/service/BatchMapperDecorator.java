package io.equalink.pricekeep.service;

import io.equalink.pricekeep.data.ProductQuoteImportBatch;
import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.data.StoreGroup;
import io.equalink.pricekeep.data.StoreImportBatch;
import io.equalink.pricekeep.service.dto.BatchMapper;
import io.equalink.pricekeep.service.dto.ProductQuoteImportBatchDTO;
import io.equalink.pricekeep.service.dto.StoreImportBatchDTO;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@Decorator
public abstract class BatchMapperDecorator implements BatchMapper {
    @Inject
    EntityManager entityManager;

    @Inject
    @Delegate
    private BatchMapper delegate;

    @Override
    public StoreImportBatch toStoreImportBatchEntity(StoreImportBatchDTO batch) {
        StoreImportBatch batchEntity = delegate.toStoreImportBatchEntity(batch);
        List<StoreGroup> storeGroupList = batch.storeGroupList().stream().map(id -> entityManager.getReference(StoreGroup.class, id)).toList();
        batchEntity.setStoreGroup(storeGroupList);

        return batchEntity;
    }

    @Override
    public ProductQuoteImportBatch toProductQuoteImportBatchEntity(ProductQuoteImportBatchDTO batch) {
        ProductQuoteImportBatch batchEntity = delegate.toProductQuoteImportBatchEntity(batch);
        List<Store> storeList = batch.storeList().stream().map(id -> entityManager.getReference(Store.class, id)).toList();
        batchEntity.setSource(storeList);

        return batchEntity;
    }
}
