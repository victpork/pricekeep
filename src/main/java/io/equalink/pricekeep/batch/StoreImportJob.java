package io.equalink.pricekeep.batch;

import io.equalink.pricekeep.service.StoreService;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@JBossLog
@DisallowConcurrentExecution
public class StoreImportJob implements Job {

    public static final String TYPE = "StoreImportJob";

    @Inject
    StoreService storeService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long jobId = context.getJobDetail().getJobDataMap().getLong(BatchController.JOB_ID);
        storeService.execImportStoreBatch(jobId);
    }
}
