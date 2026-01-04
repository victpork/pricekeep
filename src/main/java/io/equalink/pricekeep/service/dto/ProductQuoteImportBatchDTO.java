package io.equalink.pricekeep.service.dto;

import java.util.List;

public record ProductQuoteImportBatchDTO(String name, String description, String cronTrigger, List<Long> storeList, String keyword) {
}
