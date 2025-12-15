package io.equalink.pricekeep.data;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "quote_discount")
public class Discount {
    public enum Type {
        PERCENTAGE("P"),
        FIXED_AMOUNT("F"),
        BUNDLE("B"),
        NEAR_EXPIRY("E"),
        OTHER("O");

        @EnumeratedValue
        final String code;

        Type(String code) {
            this.code = code;
        }
    }

    @Id
    @Column(name = "discount_id")
    private Long id;

    @OneToOne(mappedBy = "discount", cascade = {CascadeType.ALL})
    private Quote quote;

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private Type type;

    /**
     * The difference between originalPrice and salePrice. Value meaning depends on Type.
     * if discount type is PERCENTAGE, value is the percentage off, if multibuy/bundle, the value would be the package price.
     * otherwise would be the deducted price
     * Null if there are no sale/promotion
     */
    @Column(nullable = false, name = "save_value")
    private BigDecimal saveValue;

    /**
     * Sale price as shown on shelf. Individual price if multibuy
     */
    @Column(name = "sale_price")
    private BigDecimal salePrice;

    @Column(name = "multibuy_quantity")
    private Integer multiBuyQuantity;

    /**
     * Calculate the discounted price
     * @return per-unit discounted price, ignoring conditions
     */
    public BigDecimal applyDiscount(BigDecimal originalPrice) {
        return switch (getType()) {
            case Type.PERCENTAGE -> originalPrice.multiply(saveValue);
            case Type.BUNDLE -> saveValue.divide(new BigDecimal(multiBuyQuantity), 2, RoundingMode.HALF_UP);
            case Type.FIXED_AMOUNT -> originalPrice.subtract(saveValue);
            case Type.NEAR_EXPIRY,  Type.OTHER -> saveValue;
        };
    }
}
