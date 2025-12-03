package io.equalink.pricekeep.data;

import jakarta.persistence.*;
import lombok.*;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

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
        joinColumns = @JoinColumn(name = "store_id"),
        inverseJoinColumns = @JoinColumn(name = "batch_id")
    )
    private List<Store> source;

    private String keyword;


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

    }
}
