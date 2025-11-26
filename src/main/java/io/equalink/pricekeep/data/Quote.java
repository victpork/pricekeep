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
@Cacheable
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id; 

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
    private Store quoteStore;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    private Product product;

    @Column(name = "quote_date")
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
    @OneToOne(mappedBy = "quote", orphanRemoval = true, cascade = {CascadeType.ALL})
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
     * @return
     */
    public BigDecimal getUnitPriceFromPackage(boolean useDiscount) {
        BigDecimal basePrice = price;
        if (useDiscount && discount != null) {
            basePrice = discount.applyDiscount(price);
        }
        return basePrice.divide(
            product.getPackageSize().multiply(BigDecimal.valueOf(product.getItemPerPackage())), RoundingMode.HALF_EVEN
        );
    }
}
