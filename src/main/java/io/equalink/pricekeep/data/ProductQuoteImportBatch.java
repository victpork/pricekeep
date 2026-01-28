package io.equalink.pricekeep.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "quote_import_batch")
public class ProductQuoteImportBatch extends BaseBatch {

    @ManyToMany
    @JoinTable(name = "product_quote_batch_rel",
        joinColumns = @JoinColumn(name = "batch_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "store_id", referencedColumnName = "id"),
        foreignKey =
        @ForeignKey(name = "fk_batch_id"),
        inverseForeignKey =
        @ForeignKey(name = "fk_store_id")
    )
    private List<Store> source;

    private String keyword;
}
