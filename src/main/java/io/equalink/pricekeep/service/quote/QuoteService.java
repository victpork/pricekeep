package io.equalink.pricekeep.service.quote;

import java.util.concurrent.ExecutorService;

import io.equalink.pricekeep.repo.ProductRepo;
import org.jboss.logging.Logger;

import io.equalink.pricekeep.repo.QuoteRepo;
import io.equalink.pricekeep.service.dto.QuoteMapper;
import io.quarkus.virtual.threads.VirtualThreads;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class QuoteService {

    @Inject
    @VirtualThreads
    ExecutorService workerPool;

    @Inject
    private QuoteMapper qm;

    @Inject
    private QuoteRepo quoteRepo;

    @Inject
    private ProductRepo productRepo;

    private static final Logger LOG = Logger.getLogger(QuoteService.class);
}
