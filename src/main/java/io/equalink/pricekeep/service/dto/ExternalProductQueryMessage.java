package io.equalink.pricekeep.service.dto;

import java.util.List;

public record ExternalProductQueryMessage(String keyword, List<String> sources) {
}
