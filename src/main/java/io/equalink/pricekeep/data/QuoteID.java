package io.equalink.pricekeep.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class QuoteID implements Serializable {

    private Store quoteStore;

    private Product product;

    private LocalDate quoteDate;

    public QuoteID() {

    }
}
