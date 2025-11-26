package io.equalink.pricekeep.service.dto;

import java.time.Period;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TimeRange {
    ONE_MONTH("1M", Period.ofMonths(1)),
    THREE_MONTH("1M", Period.ofMonths(3)),
    SIX_MONTH("1M", Period.ofMonths(6)),
    TWELVE_MONTH("1M", Period.ofMonths(12));

    @JsonValue
    public final String code;
    public final Period period;

    private TimeRange(String code, Period period) {
        this.code = code;
        this.period = period;
    }

}
