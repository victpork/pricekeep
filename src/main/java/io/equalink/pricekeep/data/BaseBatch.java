package io.equalink.pricekeep.data;

import io.equalink.pricekeep.batch.JobStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.quartz.Job;
import org.quartz.JobDataMap;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
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
    protected String jobType;

    @Column(name = "cron_trigger", length = 50)
    protected String cronTrigger;

    protected LocalDateTime lastRunTime;

    protected JobStatus lastRunResult;
}
