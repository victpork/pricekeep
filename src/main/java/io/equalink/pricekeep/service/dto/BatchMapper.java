package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.data.ProductQuoteImportBatch;
import io.equalink.pricekeep.data.StoreImportBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface BatchMapper {
    @Mapping(target = "storeGroup", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobType", ignore = true)
    @Mapping(target = "lastRunTime", ignore = true)
    @Mapping(target = "lastRunResult", ignore = true)
    StoreImportBatch toStoreImportBatchEntity(StoreImportBatchDTO batch);

    @Mapping(target = "source", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobType", ignore = true)
    @Mapping(target = "lastRunTime", ignore = true)
    @Mapping(target = "lastRunResult", ignore = true)
    ProductQuoteImportBatch toProductQuoteImportBatchEntity(ProductQuoteImportBatchDTO batch);
}
