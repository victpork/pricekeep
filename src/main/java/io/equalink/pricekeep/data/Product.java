package io.equalink.pricekeep.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumeratedValue;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyClass;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.*;

@Setter
@Getter
@Entity
@Table(
    name = "product",
    indexes = {
        @Index(columnList = "gtin"),
        @Index(columnList = "name"),
        @Index(columnList = "tags")
    })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Schema(enumeration = {"EA", "KG", "HG", "G", "L", "M"})
    public enum Unit {
        @JsonEnumDefaultValue
        PER_ITEM("EA"),
        PER_KG("KG"),
        PER_100G("HG"),
        PER_G("G"),
        PER_LITRE("L"),
        PER_METRE("M");

        @EnumeratedValue
        @JsonValue
        final String code;

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
    @Column(unique = true, nullable = true, length = 14)
    private String gtin;

    /**
     * Price quote detail relation, cascade all operations
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "product")
    @Builder.Default
    private List<Quote> priceQuotes = new ArrayList<>();

    /**
     * Quantity per package used for price calculation (e.g., 500ml, 1kg, etc.)
     * for discrete item (when {@link #unit} = {@link Unit#PER_ITEM}) this is null
     */
    @Column(name = "package_size")
    private BigDecimal packageSize;

    /**
     * Number of items in a bundle (e.g., 6 for a pack of 6)
     */
    @Column(nullable = false, name = "item_per_package")
    private int itemPerPackage;

    @Enumerated(EnumType.STRING)
    @Column(length = 2)
    private Unit unit;

    /**
     * storing the list of SKU used by store group respectively
     */
    @JsonIgnore
    @OneToMany(mappedBy = "product")
    @Builder.Default
    private Set<GroupProductCode> groupSKU = new HashSet<>();

    @Column(name = "tags", columnDefinition = "text[]")
    @Builder.Default
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
    @Builder.Default
    private Map<ProductStatType, BigDecimal> priceStats = new HashMap<>();


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
        groupSKU.add(gpc);
    }
}
