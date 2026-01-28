package io.equalink;

import io.equalink.pricekeep.AdminResource;
import io.equalink.pricekeep.data.StoreImportBatch;
import io.equalink.pricekeep.service.dto.ProductQuoteImportBatchDTO;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(AdminResource.class)
public class BatchImportTest {

    @Test
    @TestTransaction
    void testCreateQuoteImportBatch() {
        ProductQuoteImportBatchDTO batch = new ProductQuoteImportBatchDTO("test", "description", "0 12 15 * * ?", List.of(1L, 2L, 3L), "cola");
        given().contentType(ContentType.JSON).body(batch).when().post("/batch/newProductQuoteImport").then().statusCode(202);
    }

    @Test
    void testStoreBean() {
        StoreImportBatch t =  new StoreImportBatch();
        t.setName("test");
        t.setDescription("description");
    }
}
