package io.equalink.pricekeep.data;

public record BatchExecDetail(Long id, String name, Class<?> type, String cronTrigger) {
}
