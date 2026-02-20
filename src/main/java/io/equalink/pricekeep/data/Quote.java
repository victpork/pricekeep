package io.equalink.pricekeep.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(
    name = "quote",
    indexes = { @Index(columnList = "product_id, store_id, quote_date DESC") }
)
@IdClass(QuoteID.class)
@Cacheable
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Quote {

    public enum Source {
        USER("U"),
        SYSTEM("S");

        @EnumeratedValue
        final String code;

        Source(String code) {
            this.code = code;
        }
    }

    @ManyToOne(cascade = { CascadeType.MERGE }, optional = false)
    @JoinColumn(name = "store_id")
    @Id
    private Store quoteStore;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @Id
    private Product product;

    @Column(name = "quote_date")
    @Id
    private LocalDate quoteDate;

    /**
     * The original price without any discount
     */
    @Column(nullable = false)
    @NotNull
    private BigDecimal price;

    /**
     * Discount info, null if there are no sale/promotion
     */
    @OneToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @Enumerated(EnumType.STRING)
    @Column(length = 1, name = "quote_source")
    private Source quoteSource;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "dct_unit_price")
    private BigDecimal discountedUnitPrice;

    /**
     * Retrieve unit price from package data
     * @param useDiscount include discount
     * @return Unit price
     */
    public BigDecimal getUnitPriceFromPackage(boolean useDiscount) {
        BigDecimal basePrice = price;
        if (useDiscount && discount != null) {
            basePrice = discount.applyDiscount(price);
        }
        /*
        +-------------+----------------+------------------------------------------------------------------------------+
        | packageSize | itemPerPackage | Result                                                                       |
        +-------------+----------------+------------------------------------------------------------------------------+
        | null        | null           | Loose product, unitPrice = price                                             |
        +-------------+----------------+------------------------------------------------------------------------------+
        | not null    | null           | Single product that has volume,                                              |
        |             |                | unitPrice = price / (packageSize / unitScale)                                |
        +-------------+----------------+------------------------------------------------------------------------------+
        | null        | not null       | Bundled product with discrete item, unitPrice = price / itemPerPackage       |
        +-------------+----------------+------------------------------------------------------------------------------+
        | not null    | not null       | Bundled product with volume,                                                 |
        |             |                | unitPrice = price / (packageSize \*  itemPerPackage / unitScale)             |
        +-------------+----------------+------------------------------------------------------------------------------+
         */
        // If item sold in loose
        if (product.getPackageSize() == null && product.getItemPerPackage() == null) return basePrice;
        // Discrete item, unit price is $/ea
        if (product.getUnit() == Product.Unit.PER_ITEM) return basePrice.divide(BigDecimal.valueOf(product.getItemPerPackage()), RoundingMode.HALF_EVEN);
        // unit price = base price / (itemPerPackage * quantPerItem / unitScale)
        // e.g. a package of 6 cans of 330ml fizzy drinks sold at $ 12
        // unit price per 100ml = $12 / (330 * 6 / 100) = $0.60/100ml
        BigDecimal scale = product.getUnitScale() == null ? BigDecimal.ONE : product.getUnitScale();
        BigDecimal packageSize = (product.getPackageSize() == null) ?  BigDecimal.ONE : product.getPackageSize();
        return basePrice.divide(
            packageSize.multiply(BigDecimal.valueOf(product.getItemPerPackage())).multiply(scale), RoundingMode.HALF_EVEN
        );
    }
}
