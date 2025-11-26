package io.equalink.pricekeep.service;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PeriodLength {
    WEEK("1W"), MONTH("1M"
    ), QUARTER("3W"), YEAR("1Y");

    @JsonValue
    public final String code;

    private PeriodLength(String code) {
        this.code = code;
    }
}