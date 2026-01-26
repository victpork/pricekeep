package io.equalink.pricekeep.batch;

import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.service.StoreService;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.quartz.*;

@JBossLog
@DisallowConcurrentExecution
public class StoreImportJob extends BaseJob {

    public static final String TYPE = "StoreImportJob";

    @Inject
    StoreService storeService;

    @Override
    public void run(BaseBatch b, JobDataMap contextMap) throws JobExecutionException {
        long jobId = contextMap.getLong(BatchController.JOB_ID);
        storeService.execImportStoreBatch(jobId);
    }
}
