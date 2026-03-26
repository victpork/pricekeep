package io.equalink.pricekeep.data;

import jakarta.persistence.EnumeratedValue;

public enum StatType {
    DAY_AVG("DA"),
    MONTH_AVG("MA"),
    YEAR_AVG("YA"),
    DAY_LOW("DL"),
    MONTH_LOW("WL"),
    YEAR_LOW("YL");

    @EnumeratedValue
    public final String code;

    StatType(String code)  {
        this.code = code;
    }
}
