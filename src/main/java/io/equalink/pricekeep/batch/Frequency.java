package io.equalink.pricekeep.batch;

public enum Frequency {
    Daily("D"),
    Bidaily("B"),
    Weekly("W"),
    Monthly("M");

    final String code;
    Frequency(String code) {
        this.code = code;
    }
}
