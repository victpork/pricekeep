package io.equalink.pricekeep.service.pricefetch.woolworths;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WoolworthsStoreArea {
    private Long id;
    private String name;
    private List<WoolworthsStoreAddress> storeAddresses;
}
