package io.equalink.pricekeep.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
@Table(name = "alert")
public class Alert {
    @Id
    @OneToOne
    private Product product;

    @Column(name = "target_price")
    private BigDecimal targetPrice;
}
