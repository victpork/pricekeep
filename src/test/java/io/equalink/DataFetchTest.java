package io.equalink;

import io.equalink.pricekeep.service.pricefetch.ProductFetchService;
import io.equalink.pricekeep.service.pricefetch.ProductQuoteFetchService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class DataFetchTest {

    private static final Logger LOG = Logger.getLogger(DataFetchTest.class);

    @Inject
    ProductFetchService fetchService;

    @InjectMock
    ProductQuoteFetchService service1;

    @InjectMock
    ProductQuoteFetchService service2;

    @Test
    void testProductFetchService() {
        var serviceList = fetchService.getListOfAvailableServices();
        LOG.info(serviceList.getFirst());
        assertEquals(1, serviceList.size());
    }

    @Test
    void testFetchAll() {

    }
}
