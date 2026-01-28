package io.equalink.pricekeep.batch;

import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.repo.BatchRepo;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.narayana.jta.TransactionExceptionResult;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.quartz.*;

import java.time.LocalDateTime;

@DisallowConcurrentExecution
abstract public class BaseJob implements Job {

    @Inject
    protected BatchRepo batchRepo;

    @Override
    @ActivateRequestContext
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var contextDataMap = context.getJobDetail().getJobDataMap();
        contextDataMap.put(BatchController.JOB_STATUS, JobStatus.RUNNING.name());
        Long batchId = contextDataMap.getLong(BatchController.JOB_ID);
        batchRepo.findById(batchId).ifPresentOrElse(
            batch -> {
                int result = QuarkusTransaction.requiringNew().exceptionHandler((t) -> {
                    Log.error("Run batch error", t);
                    batchRepo.setBatchStatus(batch, JobStatus.ERROR, LocalDateTime.now());
                    return TransactionExceptionResult.COMMIT;
                }).call(() -> {
                    batchRepo.setBatchStatus(batch, JobStatus.RUNNING, LocalDateTime.now());
                    run(batch, contextDataMap);
                    return 0;
                });
                if (result == 0) {
                    QuarkusTransaction.requiringNew().run(() -> {
                        Log.infov("Run {2} batch [{0}]{1} success", batch.getId(), batch.getName(), batch.getJobType());
                        batchRepo.setBatchStatus(batch, JobStatus.COMPLETED, LocalDateTime.now());
                    });
                }
            }, () -> Log.errorv("Job name {0} with JobID {1} does not exist", context.getJobDetail().getKey(), batchId));
    }

    public abstract void run(BaseBatch b, JobDataMap contextMap) throws JobExecutionException;

}
