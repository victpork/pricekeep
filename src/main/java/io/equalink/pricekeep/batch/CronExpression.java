package io.equalink.pricekeep.batch;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class CronExpression {
    private Frequency frequency;
    private LocalTime time;

    private int dayOfMonth;

    @Override
    public String toString() {
        return switch (frequency) {
            case Daily -> String.format("0 %d %d ? * *", time.getMinute(), time.getHour());
            case Bidaily -> String.format("0 %d %d */2 * *", time.getMinute(), time.getHour());
            case Monthly -> String.format("0 %d %d %d * ?", time.getMinute(), time.getHour(), dayOfMonth);
            case Weekly -> String.format("0 %d %d * * ?", time.getMinute(), time.getHour());
        };
    }
}
