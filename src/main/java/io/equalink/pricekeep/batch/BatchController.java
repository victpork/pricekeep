package io.equalink.pricekeep.batch;

import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.data.BatchExecDetail;
import io.equalink.pricekeep.repo.BatchRepo;
import io.equalink.pricekeep.service.dto.JobInfo;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@JBossLog
public class BatchController {

    public static final String JOB_TYPE = "JOB_TYPE";
    public static final String JOB_ID = "JOB_ID";

    @Inject
    Scheduler quartzScheduler;

    @Inject
    BatchRepo batchRepo;

    void onStart(@Observes StartupEvent event) throws SchedulerException {
        List<BatchExecDetail> batchList = batchRepo.getAllBatchesForExecution();

        for (BatchExecDetail b : batchList) {
            JobBuilder jobBuilder = switch (b.jobType()) {
                case ProductQuoteImportJob.TYPE -> JobBuilder.newJob(ProductQuoteImportJob.class);
                case StoreImportJob.TYPE -> JobBuilder.newJob(StoreImportJob.class);
                default -> {
                    log.errorv("Invalid job type {0} for ID {1}", b.jobType(), b.id());
                    yield null;
                }
            };
            if (jobBuilder == null) continue;
            JobDetail job = jobBuilder
                                .withIdentity(b.name())
                                .usingJobData(JOB_TYPE, b.jobType())
                                .usingJobData(JOB_ID, b.id())
                                .storeDurably()
                                .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                                  .forJob(b.name())
                                  .withSchedule(CronScheduleBuilder.cronSchedule(b.cronTrigger()))
                                  .build();

            quartzScheduler.scheduleJob(job, trigger);
        }
    }

    public List<JobInfo> getAllJobs() {

        List<BaseBatch> batchList = batchRepo.findAll();
        return batchList.stream().map(batch -> {
            var jInfo = JobInfo.builder();
            jInfo.name(batch.getName());
            jInfo.description(batch.getDescription());
            jInfo.id(batch.getId());
            jInfo.enabled(batch.isEnabled());

            try {
                var nextTriggerTime = quartzScheduler.getTriggersOfJob(JobKey.jobKey(batch.getName()))
                                  .stream().findFirst().map(
                                      t -> LocalDateTime.ofInstant(t.getNextFireTime().toInstant(), ZoneId.systemDefault()));
                jInfo.nextExecTime(nextTriggerTime.orElse(LocalDateTime.MIN));
            } catch (SchedulerException e) {
                jInfo.nextExecTime(LocalDateTime.MIN);
            }
            return jInfo.build();
        }).toList();
    }


    public void forceStart(BaseBatch batch) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(batch.getName());

        quartzScheduler.triggerJob(jobKey);
    }
}
