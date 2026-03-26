package io.equalink.pricekeep.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="product_stats")
@Getter @Setter
@IdClass(ProductStatID.class)
public class ProductStat {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Id
    private LocalDate asOfDate;

    @Id
    @Enumerated(EnumType.STRING)
    private StatType statType;

    private BigDecimal value;
}
