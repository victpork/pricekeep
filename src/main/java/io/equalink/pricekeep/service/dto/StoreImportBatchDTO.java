package io.equalink.pricekeep.service.dto;

import java.util.List;

public record StoreImportBatchDTO(String name, String description, String cronTrigger, List<Long> storeGroupList) {
}
