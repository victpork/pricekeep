package io.equalink.pricekeep.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
public class Alert {

    @Id
    private Long id;

    @OneToOne
    private Product product;

    @Column(name = "target_price")
    private BigDecimal targetPrice;

}
