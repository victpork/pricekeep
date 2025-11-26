package io.equalink.pricekeep.service.quote;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.service.dto.ProductMapper;
import io.equalink.pricekeep.service.dto.QuoteDTO;
import jakarta.data.page.PageRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.jboss.logging.Logger;

import io.equalink.pricekeep.data.Product;
import io.equalink.pricekeep.repo.ProductRepo;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProductService {

    private static final Logger LOG = Logger.getLogger(ProductService.class);

    @Inject
    EntityManager em;

    @Inject
    private ProductRepo productRepo;

    private PatriciaTrie<String> suggestWordList;

    @Inject
    private ProductMapper productMapper;

    @Startup
    void init() {
        initSearchTrie();
    }

    public Optional<Product> getProductByGTIN(String gtin) {
        return productRepo.findByGTIN(gtin);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepo.findByIdWithLatestQuotes(id);
    }

    public List<Product> getProductByKeyword(String keyword) {
        return productRepo.findByKeyword(keyword);
    }

    void initSearchTrie() {
        LOG.info("Initialising autocomplete index trie");
        TreeMap<String, String> indexMap = new TreeMap<>();
        productRepo.getKeywordList().forEach(s -> indexMap.put(s.toLowerCase(), s));
        
        suggestWordList = new PatriciaTrie<>(indexMap);
        LOG.infov("{0} words has been inserted to the trie", suggestWordList.size());
        
    }

    public List<String> getProductListName() {
        return productRepo.getKeywordList();
    }

    public void persist(Product p) {
        productRepo.persist(p);
    }

    public List<QuoteDTO> latestDeals(int page, int pageSize) {

        var p = productRepo.getLatestDeals(PageRequest.ofPage(page, 5, false), LocalDate.now().minusDays(5));

        return p.content().stream().map(productMapper::toQuoteDTO).toList();
    }

    public List<Quote> getPriceHistory(Long productCode, Period period, boolean includeDiscount) {
        return productRepo.getPriceHistory(productCode, LocalDate.now().minus(period), includeDiscount);
    }

    public List<String> suggestWords(String input) {
        var r = suggestWordList.prefixMap(input.toLowerCase());
        return List.copyOf(r.keySet());
    }

    @Transactional
    public void quotePriceForProduct(Long productId, Quote q) {
        var p = em.find(Product.class, productId);
        if (p == null) {
            throw new EntityNotFoundException("Product ID not found: " + productId);
        }
        p.getPriceQuotes().add(q);
    }

    public void updateProductInfo(Product p) {
        productRepo.persist(p);
    }
}
