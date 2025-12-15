package io.equalink.pricekeep.batch;

import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.data.StoreImportBatch;
import io.equalink.pricekeep.repo.BatchRepo;
import io.equalink.pricekeep.repo.StoreRepo;
import io.equalink.pricekeep.service.pricefetch.ExternalImportController;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Optional;

@JBossLog
public class StoreImportJob implements Job {

    public static final String TYPE = "StoreImportJob";

    @Inject
    BatchRepo batchRepo;

    @Inject
    StoreRepo storeRepo;

    @Inject
    private ExternalImportController externalImportController;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long jobId = context.getJobDetail().getJobDataMap().getLong(BatchController.JOB_ID);
        Optional<BaseBatch> batch = batchRepo.findById(jobId);
        batch.ifPresentOrElse(b -> {
            if (b instanceof StoreImportBatch siBatch) {
                externalImportController.getStoreListFromExternalServices(siBatch.getStoreGroup())
                    .subscribe().with(store -> storeRepo.persist(store));
            } else {
                log.errorv("Batch {0}(ID:{1}) is {2} but configured as {3}", b.getName(), jobId, b.getClass().getSimpleName(), "StoreImportBatch");
            }
        }, () -> log.errorv("Batch ID {0} not found", jobId));
    }
}
