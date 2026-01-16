package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.service.dto.utils.AlertAction;

import java.math.BigDecimal;

public record SetAlertMessage(AlertAction action, BigDecimal targetPrice) {
}
