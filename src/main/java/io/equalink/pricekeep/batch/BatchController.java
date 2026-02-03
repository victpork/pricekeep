package io.equalink.pricekeep.batch;

import io.equalink.pricekeep.data.*;
import io.equalink.pricekeep.repo.BatchRepo;
import io.equalink.pricekeep.service.dto.JobInfo;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.jbosslog.JBossLog;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@JBossLog
public class BatchController {

    public static final String JOB_TYPE = "JOB_TYPE";
    public static final String JOB_ID = "JOB_ID";
    public static final String JOB_STATUS = "JOB_STATUS";

    @Inject
    Scheduler quartzScheduler;

    @Inject
    BatchRepo batchRepo;

    public void createBatch(BaseBatch batchEntity) throws SchedulerException {
        batchRepo.persist(batchEntity);
        scheduleBatch(batchEntity);
    }

    @Transactional
    public void enableBatch(BaseBatch batchEntity) throws SchedulerException {
        scheduleBatch(batchEntity);
        batchRepo.setEnabled(batchEntity,true);
    }

    @Transactional
    public void disableBatch(BaseBatch batchEntity) throws SchedulerException {
        quartzScheduler.deleteJob(batchEntity.getJobKey());
        batchRepo.setEnabled(batchEntity,false);
    }

    @Startup
    void onStart() throws SchedulerException {
        List<BaseBatch> batchList = batchRepo.getAllBatchesForExecution();
        log.infov("Batch count: {0}", batchList.size());
        batchList.forEach(batch -> log.infov("Batch: {0}[{1}]: {2}", batch.getName(), batch.getId(), batch.isEnabled()? "enabled" : "disabled"));
        quartzScheduler.clear();
        for (BaseBatch b : batchList) {
            if (b.isEnabled()) scheduleBatch(b);
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
            jInfo.type(batch.getJobType());
            jInfo.frequency(batch.getCronTrigger());
            jInfo.lastRunTime(batch.getLastRunTime());
            jInfo.enabled(batch.isEnabled());
            jInfo.lastResult(batch.getLastRunResult());
            if (batch.isEnabled()) {
                try {
                    var nextTriggerTime = quartzScheduler.getTriggersOfJob(batch.getJobKey())
                                              .stream().findFirst().map(
                            t -> LocalDateTime.ofInstant(t.getNextFireTime().toInstant(), ZoneId.systemDefault()));
                    jInfo.nextExecTime(nextTriggerTime.orElse(LocalDateTime.MIN));
                    jInfo.status(JobStatus.valueOf(quartzScheduler.getJobDetail(batch.getJobKey()).getJobDataMap().getString(JOB_STATUS)));
                } catch (SchedulerException e) {
                    jInfo.nextExecTime(LocalDateTime.MIN);
                }
            } else {
                jInfo.status(JobStatus.DISABLED);
            }
            switch (batch) {
                case StoreImportBatch siBatch:
                    jInfo.parameters(Map.of("storegroup", siBatch.getStoreGroup().stream().map(StoreGroup::getName).collect(Collectors.joining(","))));
                    break;
                case ProductQuoteImportBatch pqBatch:
                    jInfo.parameters(Map.of("keyword", pqBatch.getKeyword(),
                        "store", pqBatch.getSource().stream().map(Store::getName).collect(Collectors.joining(","))));
                    break;
                default:
                   break;
            }
            return jInfo.build();
        }).toList();
    }

    public void forceStart(BaseBatch batch) throws SchedulerException {
        JobKey jobKey = batch.getJobKey();
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
    }

    private void scheduleBatch(BaseBatch b) throws SchedulerException {
        JobBuilder jobBuilder = switch (b) {
            case ProductQuoteImportBatch _ -> JobBuilder.newJob(ProductQuoteImportJob.class);
            case StoreImportBatch _ -> JobBuilder.newJob(StoreImportJob.class);
            default -> {
                log.errorv("Invalid job type {0} for ID {1}", b.getClass().getSimpleName(), b.getId());
                yield null;
            }
        };
        if (jobBuilder == null) return;
        JobDetail job = jobBuilder
                            .withIdentity(b.getJobKey())
                            .usingJobData(JOB_TYPE, b.getClass().getSimpleName())
                            .usingJobData(JOB_ID, b.getId())
                            .usingJobData(JOB_STATUS, JobStatus.ENABLED.name())
                            .storeDurably()
                            .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                              .forJob(b.getJobKey())
                              .withSchedule(CronScheduleBuilder.cronSchedule(b.getCronTrigger()))
                              .build();

        quartzScheduler.scheduleJob(job, trigger);
    }
}
