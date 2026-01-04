package io.equalink.pricekeep.service.dto;

import io.equalink.pricekeep.data.Discount;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.Store;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.extern.jbosslog.JBossLog;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Decorator
@JBossLog
public abstract class QuoteMapperDecorator implements QuoteMapper {
    @Inject
    EntityManager em;

    @Inject @Delegate @Any
    QuoteMapper mapper;

    @Inject
    StoreMapper storeMapper;

    @Override
    public Quote toEntity(QuoteDTO qInfo) {
        var quote = mapper.toEntity(qInfo);
        if (qInfo.getStoreInfo() instanceof BaseEntity.WithId<StoreInfo>(Long id)) {
            log.infov("QuoteDTO with ID {0}", id);
            quote.setQuoteStore(em.find(Store.class, id));
        } else if (qInfo.getStoreInfo() instanceof BaseEntity.WithDetail<StoreInfo>(StoreInfo storeInfo)){
            log.infov("QuoteDTO with StoreInfo {0}", storeInfo.name());
            Store s = storeMapper.toEntity(storeInfo);
            quote.setQuoteStore(s);
        }
        if (qInfo.getDiscountType() != null) {
            if (qInfo.getDiscountType().equals(Discount.Type.BUNDLE)) {
                quote.getDiscount().setSaveValue(qInfo.getPrice().subtract(qInfo.getSalePrice().divide(new BigDecimal(qInfo.getMultibuyQuantity()), RoundingMode.HALF_UP)));
            } else {
                quote.getDiscount().setSaveValue(qInfo.getPrice().subtract(qInfo.getSalePrice()));
            }
            quote.getDiscount().setQuote(quote);
        } else {
            quote.setDiscount(null);
        }

        return quote;
    }
}
