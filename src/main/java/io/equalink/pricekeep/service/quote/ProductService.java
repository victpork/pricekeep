package io.equalink.pricekeep.service.quote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import io.equalink.pricekeep.data.Alert;
import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.data._Product;
import io.equalink.pricekeep.repo.AlertRepo;
import io.equalink.pricekeep.repo.QuoteRepo;
import io.equalink.pricekeep.service.dto.CompactQuote;
import io.equalink.pricekeep.service.dto.ProductMapper;
import io.equalink.pricekeep.service.dto.QuoteDTO;
import io.quarkus.logging.Log;
import jakarta.data.page.Page;
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

    @Inject
    private QuoteRepo quoteRepo;

    private PatriciaTrie<String> suggestWordList;

    @Inject
    private ProductMapper productMapper;

    @Inject
    AlertRepo alertRepo;

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

    public Page<Product> getAllProduct(Integer page, Integer pageSize, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return productRepo.findAll(_Product.name.ascIgnoreCase(), PageRequest.ofPage(page).size(pageSize));
        }
        return productRepo.findAll("%" + keyword + "%", _Product.name.ascIgnoreCase(), PageRequest.ofPage(page).size(pageSize));
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

        var p = productRepo.getLatestDeals(PageRequest.ofPage(page, pageSize, false), LocalDate.now().minusDays(5));

        return p.content().stream().map(productMapper::toQuoteDTO).toList();
    }

    public List<CompactQuote> getPriceHistory(Long productCode, Period period) {
        //return productRepo.getPriceHistory(productCode, LocalDate.now().minus(period), includeDiscount);
        var cutOff = LocalDate.now().minus(period);
        Log.infov("Cutoff date: {0}", cutOff);
        return quoteRepo.findLowestQuotePerDayHistByProduct(productCode, cutOff);
    }

    public List<String> suggestWords(String input) {
        var r = suggestWordList.prefixMap(input.toLowerCase());
        return List.copyOf(r.keySet());
    }

    @Transactional
    public void quotePriceForProduct(Long productId, Quote q) {

        var p = em.getReference(Product.class, productId);
        q.setProduct(p);
        if (q.getUnitPrice() == null) {
            p = em.find(Product.class, productId);
            q.setProduct(p);
            q.setUnitPrice(q.getUnitPriceFromPackage(false));
        }
        em.persist(q);
    }

    public void updateProductInfo(Product p) {
        productRepo.persist(p);
    }

    public void createAlert(Long productId, BigDecimal priceLevel) {
        Product ref = em.getReference(Product.class, productId);
        Alert alert = new Alert();
        alert.setProduct(ref);
        alert.setTargetPrice(priceLevel);
        alertRepo.createAlert(alert);
    }

    public List<Product> getTriggeredAlerts() {
        return productRepo.getTriggeredAlerts();
    }
}
