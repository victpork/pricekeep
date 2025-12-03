package io.equalink.pricekeep.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jbosslog.JBossLog;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

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
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.infov("Job {0} started", this.getName());

    }
}
