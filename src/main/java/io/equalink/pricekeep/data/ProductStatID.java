package io.equalink.pricekeep.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
public class ProductStatID implements Serializable {
    private Product product;

    private LocalDate asOfDate;

    private StatType statType;

    public ProductStatID() {}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ProductStatID p) {
            if (p.getStatType() == statType && Objects.equals(asOfDate,  p.getAsOfDate()) &&
                    Objects.equals(p.getProduct().getId(), this.product.getId())) return true;
        }
        return super.equals(obj);
    }
}
