package io.equalink.pricekeep.batch;

import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.repo.BatchRepo;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.narayana.jta.TransactionExceptionResult;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
                QuarkusTransaction.requiringNew().run(() -> markJobStatus(batch, JobStatus.RUNNING));
                int result = QuarkusTransaction.requiringNew().exceptionHandler((t) -> {
                    Log.error("Run batch error", t);
                    markJobStatus(batch, JobStatus.ERROR);
                    return TransactionExceptionResult.COMMIT;
                }).call(() -> {
                    run(batch, contextDataMap);
                    return 0;
                });
                if (result == 0) {
                    QuarkusTransaction.requiringNew().run(() -> {
                        Log.infov("Run {2} batch [{0}]{1} success", batch.getId(), batch.getName(), batch.getClass().getSimpleName());
                        markJobStatus(batch, JobStatus.COMPLETED);
                    });
                }
            }, () -> Log.errorv("Job name {0} with JobID {1} does not exist", context.getJobDetail().getKey(), batchId));
    }

    public abstract void run(BaseBatch b, JobDataMap contextMap) throws JobExecutionException;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected void markJobStatus(BaseBatch batch, JobStatus status) {
        batchRepo.setBatchStatus(batch, status, LocalDateTime.now());
    }

}
