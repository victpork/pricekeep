package io.equalink.pricekeep.service;

import io.equalink.pricekeep.repo.BatchRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BatchService {
    @Inject
    BatchRepo batchRepo;

    public void runBatchJob() {
        // Implement batch job logic here
    }
}
