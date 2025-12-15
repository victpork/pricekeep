package io.equalink.pricekeep.data;

import io.equalink.pricekeep.batch.ProductQuoteImportJob;
import io.equalink.pricekeep.batch.StoreImportJob;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jbosslog.JBossLog;
import org.quartz.JobDataMap;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@JBossLog
@Entity
@Table(name = "store_batch")
public class StoreImportBatch extends BaseBatch {

    @ManyToMany
    @JoinTable(name = "store_group_batch_rel",
        joinColumns = @JoinColumn(name = "store_group_id"),
        inverseJoinColumns = @JoinColumn(name = "batch_id")
    )
    private List<StoreGroup> storeGroup;

    @Override
    protected String defaultJobType() {
        return StoreImportJob.TYPE;
    }
}
