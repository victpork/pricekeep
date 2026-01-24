package io.equalink.pricekeep.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.*;

@Setter
@Getter
@Entity
@Table(
    name = "product",
    indexes = {
        @Index(columnList = "gtin", unique = true),
        @Index(columnList = "name"),
        @Index(columnList = "tags")
    })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Schema(enumeration = {"EA", "KG", "ML", "G", "L", "M"})
    public enum Unit {
        @JsonEnumDefaultValue
        PER_ITEM("EA"),
        PER_KG("KG"),
        PER_G("G"),
        PER_MILLILITRE("ML"),
        PER_LITRE("L"),
        PER_METRE("M");

        @EnumeratedValue
        @JsonValue
        final public String code;

        Unit(String code) {
            this.code = code;
        }

        @JsonCreator
        public static Unit fromCode(String code) {
            for (Unit unit : Unit.values()) {
                if (unit.code.equals(code)) {
                    return unit;
                }
            }
            throw new IllegalArgumentException("Unknown unit code: " + code);
        }
    }

    /**
     * Statistics types for product price statistics
     */
    public enum ProductStatType {
        MONTH_AVG("M_AVG"),
        MONTH_MIN("M_MIN"),
        QUARTER_AVG("Q_AVG"),
        QUARTER_MIN("Q_MIN"),
        YEAR_AVG("Y_AVG"),
        YEAR_MIN("Y_MIN");

        @EnumeratedValue
        public final String code;

        ProductStatType(String code) {
            this.code = code;
        }
    }

    /**
     * Product ID, primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(length = 100)
    private String name;

    private String description;

    /**
     * Global Trade Item Number (GTIN) or GS1
     */
    @NaturalId
    @Column(unique = true, length = 14)
    private String gtin;

    /**
     * Price quote detail relation, cascade all operations
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "product")
    private List<Quote> priceQuotes = new ArrayList<>();

    /**
     * Quantity per package used for price calculation (e.g., 500ml, 1kg, etc.)
     * for discrete item (when {@link #unit} = {@link Unit#PER_ITEM}) this is null
     */
    @Column(name = "package_size")
    private BigDecimal packageSize;

    /**
     * Number of items in a bundle (e.g., 6 for a pack of 6)
     * Null when product price in loose form (e.g. per kg)
     */
    @Column(name = "item_per_package")
    private Integer itemPerPackage;

    @Enumerated(EnumType.STRING)
    @Column(length = 2)
    private Unit unit;


    private BigDecimal unitScale;

    /**
     * storing the list of SKU used by store group respectively
     */
    @JsonIgnore
    @OneToMany(mappedBy = "product")
    private Set<GroupProductCode> groupSKU = new HashSet<>();

    @Column(name = "tags", columnDefinition = "text[]")
    @Type(ListArrayType.class)
    private Set<String> tags = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "product_stats",
        joinColumns = {
            @JoinColumn(name = "product_id", referencedColumnName = "id"),
        }
    )
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "statType")
    @Column(name = "value", nullable = false, length = 5)
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    private Map<ProductStatType, BigDecimal> priceStats =  new HashMap<>();


    private String imgPath;

    public void updateStats(Map<ProductStatType, BigDecimal> newMetrics) {
        if (this.priceStats == null) {
            this.priceStats = new EnumMap<>(
                ProductStatType.class
            );
        }

        // Clear and replace for complete update
        this.priceStats.clear();
        this.priceStats.putAll(newMetrics);
    }

    public void addQuote(Quote q) {
        if (priceQuotes == null) priceQuotes = new ArrayList<>();

        priceQuotes.add(q);
    }

    public void addSKUMapping(GroupProductCode gpc) {
        if (groupSKU == null) groupSKU = new HashSet<>();
        groupSKU.add(gpc);
    }

    @Override
    public String toString() {
        int priceQuoteCnt = this.priceQuotes == null ? 0 : this.priceQuotes.size();
        int priceStatCnt = this.priceStats == null ? 0 : this.priceStats.size();
        int groupSKUCnt = this.groupSKU == null ? 0 : this.groupSKU.size();
        return String.format("%s[%s] - quote:%d priceStats:%d skuMap:%d",
            this.getName(),
            this.getGtin(),
            priceQuoteCnt,
            priceStatCnt,
            groupSKUCnt);
    }
}
