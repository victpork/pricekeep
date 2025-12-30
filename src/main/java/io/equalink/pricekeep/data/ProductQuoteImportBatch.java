package io.equalink.pricekeep.data;

import io.equalink.pricekeep.batch.ProductQuoteImportJob;
import jakarta.persistence.*;
import lombok.*;
import org.quartz.JobDataMap;

import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "quote_import_batch")
public class ProductQuoteImportBatch extends BaseBatch {

    @ManyToMany
    @JoinTable(name = "product_quote_batch_rel",
        joinColumns = @JoinColumn(name = "batch_id"),
        inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    private List<Store> source;

    private String keyword;
}
