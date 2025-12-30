package io.equalink.pricekeep.service.pricefetch.paknsave;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaknSaveProductSearchResult {
    List<PakNSaveProductQuote> products;
    Integer totalPages;
    Integer totalHits;
    Integer page;
}
