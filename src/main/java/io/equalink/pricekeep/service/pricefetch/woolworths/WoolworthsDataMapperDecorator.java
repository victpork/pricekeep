package io.equalink.pricekeep.service.pricefetch.woolworths;

import io.equalink.pricekeep.data.Product;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.repo.ProductRepo;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

@Decorator
public abstract class WoolworthsDataMapperDecorator implements WoolworthsDataMapper {

    @Inject
    @Delegate
    private WoolworthsDataMapper dataMapper;

    @Inject
    private ProductRepo productRepo;

    @Override
    public Quote toQuote(WoolworthsProductQuote pq) {
        return dataMapper.toQuote(pq);
    }

    @Override
    public Product toProduct(WoolworthsProductQuote pq) {
        var p = productRepo.findBySKUOrGTIN(pq.getBarcode(), pq.getSku(), "woolworths");
        return p.orElse(dataMapper.toProduct(pq));
    }
}
