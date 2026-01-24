package io.equalink.pricekeep.batch;

import io.equalink.pricekeep.data.*;
import io.equalink.pricekeep.repo.BatchRepo;
import io.equalink.pricekeep.repo.ProductRepo;
import io.equalink.pricekeep.repo.QuoteRepo;
import io.equalink.pricekeep.repo.StoreRepo;
import io.equalink.pricekeep.service.pricefetch.ExternalImportController;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.MultiOnItem;
import io.smallrye.mutiny.helpers.spies.MultiOnItemSpy;
import io.smallrye.mutiny.helpers.spies.MultiOnRequestSpy;
import io.smallrye.mutiny.helpers.spies.Spy;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.jbosslog.JBossLog;
import org.hibernate.exception.GenericJDBCException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.Duration;
import java.util.Optional;

@JBossLog
public class ProductQuoteImportJob implements Job {

    public static final String TYPE = "ProductQuoteImportJob";

    @Inject
    private ProductRepo productRepo;

    @Inject
    private ExternalImportController externalImportController;

    @Inject
    private BatchRepo batchRepo;

    @Override
    @ActivateRequestContext
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var contextDataMap = context.getJobDetail().getJobDataMap();

        Long batchId = contextDataMap.getLong(BatchController.JOB_ID);
        batchRepo.findById(batchId).ifPresentOrElse(batch -> {
            if (batch instanceof ProductQuoteImportBatch pqBatch)
                handleProductQuoteImportBatch(pqBatch);
        }, () -> log.errorv("Job name {0} with JobID {1} does not exist", context.getJobDetail().getKey(), batchId));

    }

    private void handleProductQuoteImportBatch(ProductQuoteImportBatch batch) {

        externalImportController.getProductQuoteFromExternalServices(batch.getKeyword(), batch.getSource())
            .onFailure().retry()
            .withBackOff(Duration.ofSeconds(1), Duration.ofSeconds(3))
            .atMost(2)
            .onItem().transformToUniAndConcatenate((p) -> Uni.createFrom().item(p)
                                                              .invoke(this::persistProductAndQuote)
                                                              .onFailure().recoverWithNull())
            .subscribe().with(i -> log.infov("ProductQuote {0}[{1}] written to DB", i.getProduct().getName(), i.getProduct().getGtin()),
                () -> log.info("Import job finished"));
    }

    @Transactional
    protected void persistProductAndQuote(Quote q) {

        // Quote is expected to be always new and unique, no need recovery
        try {
            productRepo.persist(q);
        } catch (GenericJDBCException e) {
            log.error("Error inserting quote", e);
        }
    }
}
