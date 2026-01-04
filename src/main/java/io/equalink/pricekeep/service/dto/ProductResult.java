package io.equalink.pricekeep.service.dto;

import java.util.List;

public record ProductResult(List<ProductInfo> productList, Long totalRecords, Long totalPages, Integer pageSize, Long currentPage) {
}
