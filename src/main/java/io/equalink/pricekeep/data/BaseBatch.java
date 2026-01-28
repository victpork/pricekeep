package io.equalink.pricekeep.data;

import io.equalink.pricekeep.batch.JobStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobKey;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(length = 20)
    private String name;
    @Column(length = 100)
    private String description;

    private boolean enabled;

    @Column(name = "job_type", length = 30)
    private String jobType;

    @Column(name = "cron_trigger", length = 50)
    private String cronTrigger;

    private LocalDateTime lastRunTime;

    private JobStatus lastRunResult;

    public JobKey getJobKey() {
        return JobKey.jobKey(name + "-" + id, jobType);
    }
}
