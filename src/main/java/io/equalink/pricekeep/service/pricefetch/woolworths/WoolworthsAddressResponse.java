package io.equalink.pricekeep.service.pricefetch.woolworths;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class WoolworthsAddressResponse {
    @JsonDeserialize(using = WoolworthsAreaDeserializer.class)
    private WoolworthsStoreArea storeAreas;
}
