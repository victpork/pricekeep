package io.equalink.pricekeep.repo;

import io.equalink.pricekeep.data.Quote;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;

@Repository
public interface QuoteRepo {

    @Save
    void persist(Quote q);




}