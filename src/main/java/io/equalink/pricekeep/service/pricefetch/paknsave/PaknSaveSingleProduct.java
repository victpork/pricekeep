package io.equalink.pricekeep.service.pricefetch.paknsave;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaknSaveSingleProduct {
    List<String> EANs;
    String ImageUrl;
    String Description;
    String name;
}
