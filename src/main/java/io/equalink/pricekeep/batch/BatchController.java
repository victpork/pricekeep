package io.equalink.pricekeep.batch;

import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.data.ProductQuoteImportBatch;
import io.equalink.pricekeep.data.StoreImportBatch;
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

@ApplicationScoped
@JBossLog
public class BatchController {

    public static final String JOB_TYPE = "JOB_TYPE";
    public static final String JOB_ID = "JOB_ID";

    @Inject
    Scheduler quartzScheduler;

    @Inject
    BatchRepo batchRepo;

    void onStart(@Observes StartupEvent ignoredEvent) throws SchedulerException {
        List<BaseBatch> batchList = batchRepo.getAllBatchesForExecution();

        for (BaseBatch b : batchList) {
            JobBuilder jobBuilder = switch (b) {
                case ProductQuoteImportBatch _ -> JobBuilder.newJob(ProductQuoteImportJob.class);
                case StoreImportBatch _ -> JobBuilder.newJob(StoreImportJob.class);
                default -> {
                    log.errorv("Invalid job type {0} for ID {1}", b.getClass().getSimpleName(), b.getId());
                    yield null;
                }
            };
            if (jobBuilder == null) continue;
            JobDetail job = jobBuilder
                                .withIdentity(b.getName())
                                .usingJobData(JOB_TYPE, b.getClass().getSimpleName())
                                .usingJobData(JOB_ID, b.getId())
                                .storeDurably()
                                .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                                  .forJob(b.getName())
                                  .withSchedule(CronScheduleBuilder.cronSchedule(b.getCronTrigger()))
                                  .build();

            quartzScheduler.scheduleJob(job, trigger);
        }
        quartzScheduler.start();
        log.info(quartzScheduler.getMetaData().getSummary());
        log.infov("Registered jobs: {0}", quartzScheduler.getJobKeys(GroupMatcher.anyJobGroup()).size());
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
        log.infov("Triggering job {0}, exists: {1}", batch.getName(), quartzScheduler.checkExists(jobKey));
        JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey);
        log.infov("Job class: {0}", jobDetail.getJobClass().getName());
        try {
            quartzScheduler.triggerJob(jobKey);

        } catch (SchedulerException e) {
            log.error(e);
        }
        log.infov("Job {0} triggered successfully", batch.getName());
        log.info(quartzScheduler.getMetaData().getSummary());
        log.info(quartzScheduler.getJobDetail(jobKey).isDurable());
    }
}
