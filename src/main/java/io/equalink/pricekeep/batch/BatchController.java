package io.equalink.pricekeep.batch;

import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.repo.BatchRepo;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.quartz.*;

import java.util.List;

@ApplicationScoped
public class BatchController {
    @Inject
    Scheduler quartzScheduler;

    @Inject
    BatchRepo batchRepo;

    void onStart(@Observes StartupEvent event) throws SchedulerException {
        List<BaseBatch> batchList = batchRepo.findAll();

        for (BaseBatch b: batchList) {
            JobDetail job = JobBuilder.newJob(BaseBatch.class)
                                .withIdentity(b.getName(), "myGroup")
                                .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                                  .withIdentity("myTrigger", "myGroup")
                                  .startNow()
                                  .withSchedule(CronScheduleBuilder.cronSchedule(b.getCronTrigger()))
                                  .build();
            quartzScheduler.scheduleJob(job, trigger);
        }
    }
}
