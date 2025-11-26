package io.equalink.pricekeep.service;

import io.equalink.pricekeep.data.Store;
import io.equalink.pricekeep.repo.StoreRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class StoreService {
    @Inject
    StoreRepo storeRepo;

    public void createStore(Store s) {
        storeRepo.persist(s);
    }

    public Optional<Store> getStoreById(Long id) {
        return storeRepo.findById(id);
    }
}
