package io.equalink.pricekeep.service.pricefetch.woolworths;

import io.equalink.pricekeep.data.GroupProductCode;
import io.equalink.pricekeep.data.Product;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data.StoreGroup;
import io.equalink.pricekeep.repo.ProductRepo;
import io.equalink.pricekeep.repo.QuoteRepo;
import io.equalink.pricekeep.repo.StoreRepo;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.regex.Matcher;

@Decorator
public abstract class WoolworthsDataMapperDecorator implements WoolworthsDataMapper {

    @Inject
    @Delegate
    private WoolworthsDataMapper dataMapper;

    @Inject
    private StoreRepo storeRepo;

    private StoreGroup wwStoreGrp;

    @PostConstruct
    void onStart() {
        Optional<StoreGroup> wwStoreGroup = storeRepo.findStoreGroupByName("woolworths");
        if  (wwStoreGroup.isEmpty()) {
            Log.error("Store group woolworths not found");
            throw new RuntimeException("Missing storegroup settings");
        }
        wwStoreGrp = wwStoreGroup.get();
    }


    @Override
    public Quote toQuote(WoolworthsProductQuote pq) {
        return dataMapper.toQuote(pq);
    }

    @Override
    public Product toProduct(WoolworthsProductQuote pq) {
        //var p = productRepo.findBySKUOrGTIN(pq.getBarcode(), pq.getSku(), "woolworths");
        var result = dataMapper.toProduct(pq);

        // Set group product code
        result.addSKUMapping(GroupProductCode.builder().internalCode(pq.getSku()).storeGroup(wwStoreGrp).build());

        String unitOfMeasureStr = pq.getSize().getCupMeasure();
        if (unitOfMeasureStr != null) {
            Matcher m = unitNumberPattern.matcher(unitOfMeasureStr);
            if (m.find()) {
                String unit = m.group("unit");
                result.setUnit(switch (unit.toLowerCase()) {
                    case "ea" -> Product.Unit.PER_ITEM;
                    case "kg" -> Product.Unit.PER_KG;
                    case "m" -> Product.Unit.PER_METRE;
                    case "ml" -> Product.Unit.PER_MILLILITRE;
                    case "l" -> Product.Unit.PER_LITRE;
                    case "g" -> Product.Unit.PER_G;
                    default -> null;
                });
                result.setUnitScale(new BigDecimal(m.group("number")));
            }
        }
        // Determine product size and items in package
        if (pq.getSize().getVolumeSize() != null && !pq.getSize().getVolumeSize().isEmpty()) {
            String volSize = pq.getSize().getVolumeSize().trim().toLowerCase();
            int packPos = volSize.indexOf("pack");
            if (packPos >= 0) {
                //Packaged product with multiple containers
                int packCnt;
                try {
                    // Try fetch number of packages. Set to 1 if fail
                    packCnt = Integer.parseInt(volSize.substring(0, packPos).trim());
                } catch (NumberFormatException e) {
                    Log.warnv("Cannot parse package size text {0} for {1}, setting to 1", packPos, pq.getName());
                    packCnt = 1;
                }
                result.setItemPerPackage(packCnt);
                if (pq.getSize().getPackageType() != null && !pq.getSize().getPackageType().isEmpty()) {
                    String packageType = pq.getSize().getPackageType().toLowerCase();
                    Matcher m = unitNumberPattern.matcher(packageType);
                    if (m.find()) {
                        result.setPackageSize(new BigDecimal(m.group("number")));
                    } else {
                        Log.warnv("Cannot determine volume for {0} with string {1} ", pq.getName(), pq.getSize().getPackageType());
                    }
                } else {
                    Log.warnv("Cannot determine volume size for product {0}, volumeSize is empty", pq.getName());
                }
            } else {
                //Product in single container
                result.setItemPerPackage(1);
                Matcher m = unitNumberPattern.matcher(volSize);
                if (m.find()) {
                    result.setPackageSize(new BigDecimal(m.group("number")));
                } else {
                    Log.warnv("Cannot determine volume for {0} with string {1} ", pq.getName(), pq.getSize().getVolumeSize());
                }
            }
        }

        return result;
    }
}
