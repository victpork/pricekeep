package io.equalink.pricekeep.batch;

import io.equalink.pricekeep.data.*;
import io.equalink.pricekeep.repo.BatchRepo;
import io.equalink.pricekeep.repo.ProductRepo;
import io.equalink.pricekeep.service.pricefetch.ExternalImportController;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.jbosslog.JBossLog;
import org.hibernate.exception.GenericJDBCException;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;

import java.time.Duration;

@JBossLog
public class ProductQuoteImportJob extends BaseJob {

    public static final String TYPE = "ProductQuoteImportJob";

    @Inject
    private ProductRepo productRepo;

    @Inject
    private ExternalImportController externalImportController;

    @Inject
    private BatchRepo batchRepo;

    @Override
    @ActivateRequestContext
    public void run(BaseBatch batch, JobDataMap contextMap) throws JobExecutionException {
        if (batch instanceof ProductQuoteImportBatch pqBatch) {
            handleProductQuoteImportBatch(pqBatch);
        } else {
            throw new JobExecutionException("Unsupported batch type: " + batch.getClass());
        }
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
                () -> log.infov("Import job {0} finished", batch.getName()));
    }

    @Transactional
    protected void persistProductAndQuote(Quote q) {

        // Quote is expected to be always new and unique, duplicate key error must be due to duplicated keys
        try {
            productRepo.persist(q);
        } catch (GenericJDBCException e) {
            log.error("Error inserting quote", e);
        }
    }
}
