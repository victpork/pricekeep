package io.equalink.pricekeep.service.pricefetch.woolworths;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class PickupAddressMessage {
    @JsonAlias("isSuccessful")
    private boolean successful;
    private String message;

}
