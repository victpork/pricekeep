package io.equalink.pricekeep.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
public class QuoteID implements Serializable {

    private Store quoteStore;

    private Product product;

    private LocalDate quoteDate;

    public QuoteID() {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof QuoteID q) {
            if (Objects.equals(q.getQuoteStore().getId(), this.quoteStore.getId()) &&
                    Objects.equals(q.getProduct().getId(), this.product.getId()) ) return true;
        }
        return super.equals(obj);
    }


}
