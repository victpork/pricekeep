package io.equalink.pricekeep.batch;

import io.equalink.pricekeep.data.Product;
import io.equalink.pricekeep.data.ProductQuoteImportBatch;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.StoreImportBatch;
import io.equalink.pricekeep.repo.BatchRepo;
import io.equalink.pricekeep.repo.ProductRepo;
import io.equalink.pricekeep.repo.StoreRepo;
import io.equalink.pricekeep.service.pricefetch.ExternalImportController;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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
            .subscribe().with(this::persistProductAndQuote);
    }

    private void persistProductAndQuote(Quote q) {
        // Check if product already exists in DB
        Product p = q.getProduct();

        if (productRepo.findByGTIN(p.getGtin()).isEmpty()) {
            productRepo.persist(p);
        }
        var prdGrpRel = p.getGroupSKU().stream().findFirst();
        prdGrpRel.ifPresent(rel -> {
            var gpCodePresent = productRepo.findBySKUOrGTIN(null, rel.getInternalCode(), rel.getStoreGroup().getName());
            if (gpCodePresent.isEmpty()) {
                productRepo.persist(p);
            }
        });
        productRepo.persist(q);

    }
}
