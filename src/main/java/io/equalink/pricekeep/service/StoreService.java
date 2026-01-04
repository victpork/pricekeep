package io.equalink.pricekeep.service;

import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.data.StoreImportBatch;
import io.equalink.pricekeep.repo.BatchRepo;
import io.equalink.pricekeep.repo.StoreRepo;
import io.equalink.pricekeep.service.pricefetch.ExternalImportController;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.jbosslog.JBossLog;

import java.util.List;
import java.util.Optional;

@JBossLog
@ApplicationScoped
public class StoreService {
    @Inject
    StoreRepo storeRepo;

    @Inject
    BatchRepo batchRepo;

    @Inject
    private ExternalImportController externalImportController;

    public void createStore(Store s) {
        storeRepo.persist(s);
    }

    public Optional<Store> getStoreById(Long id) {
        return storeRepo.findById(id);
    }

    @Transactional
    public void execImportStoreBatch(Long jobId) {
        log.infov("Executing StoreImportJob with JobID {0}", jobId);
        var batch = batchRepo.findById(jobId);
        batch.ifPresentOrElse(b -> {
            if (b instanceof StoreImportBatch siBatch) {
                String groupName = siBatch.getStoreGroup().getFirst().getName();
                log.infov("Importing branches with group ID {0}", groupName);
                // Get a list of internal IDs
                List<String> intStoreCodeList = storeRepo.findIntIDsStoreGroup(groupName);
                externalImportController.getStoreListFromExternalServices(siBatch.getStoreGroup())
                    .subscribe().with(store -> {
                        if (!intStoreCodeList.contains(store.getInternalId())) {
                            storeRepo.persist(store);
                        }
                    }, e -> {
                        log.error(e);
                    });
            } else {
                log.errorv("Batch {0}(ID:{1}) is {2} but configured as {3}", b.getName(), jobId, b.getClass().getSimpleName(), "StoreImportBatch");
            }
        }, () -> log.errorv("Batch ID {0} not found", jobId));
    }
}
