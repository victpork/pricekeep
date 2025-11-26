package io.equalink;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.equalink.pricekeep.ProductResource;
import io.equalink.pricekeep.data.Product;
import io.equalink.pricekeep.service.dto.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@QuarkusTest
@TestHTTPEndpoint(ProductResource.class)
public class PricekeepResourceTest {

    @Inject
    ObjectMapper om;
    /**
     * Endpoint to get quotes for product with max(date)
     */
    @Test
    void testGetLatestQuote() {
        ProductPageMsg msg = given().contentType(ContentType.JSON).when().get("/{productId}/quoteHist", "1").then().statusCode(200).extract().as(ProductPageMsg.class);
        Assertions.assertEquals("Milk", msg.name());
        Assertions.assertNull(msg.description());
        Assertions.assertEquals(4, msg.quotes().size());
    }

    @Test
    void testUpdateQuote() {
        QuoteDTO qInfo = new QuoteDTO(null, null, new BaseEntity.WithId<>(1L), LocalDate.now(), new BigDecimal("2.99"), null);
        given().contentType(ContentType.JSON).body(qInfo).when().post("/{productId}/quote", "1")
                .then().statusCode(RestResponse.StatusCode.ACCEPTED).body("id", notNullValue());
    }

    @Test
    void testInsertProduct() {
        // Test product with invalid GTIN
        ProductInfo pInfo = new ProductInfo(null, "Test Product", null, "Test product insertion", "412412144241266", Product.Unit.PER_ITEM, null, null, 1, 1,Set.of("test"));
        given().contentType(ContentType.JSON).body(pInfo).when().post("/new")
                .then().statusCode(RestResponse.StatusCode.BAD_REQUEST).body("violations[0].message", containsString("GTIN"));

        // Test valid product insertion
        ProductInfo pInfo2 = new ProductInfo(null, "Test Product", null, "Test product insertion", "4124121442412", Product.Unit.PER_ITEM, null, null,  1,1,Set.of("test"));
        given().contentType(ContentType.JSON).body(pInfo2).when().post("/new")
                .then().statusCode(RestResponse.StatusCode.CREATED).body("id", notNullValue());
    }

    @Test
    void testGetProductById() {
        given().contentType(ContentType.JSON).when().get("{productId}", "1")
                .then().statusCode(RestResponse.StatusCode.OK)
                .body("id", equalTo(1))
                .body("name", equalTo("Milk"))
                .body("stats", anEmptyMap())
                .body("latestQuotes", hasSize(5));
    }

    @Test
    void testGetExtResource() {
        ExternalProductQueryMessage req = new ExternalProductQueryMessage("cat food", List.of("animates"));
        given().config(RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig().setParam("http.connection.timeout", 2000)))
                .contentType(ContentType.JSON).body(req).when().post("/searchExt")
                .then().statusCode(RestResponse.StatusCode.OK).body("results", hasSize(1272));
    }

    @Test
    void testEntityResult() throws IOException {
        QuoteDTO q1 = new QuoteDTO(null, null, new BaseEntity.WithId<>(3L), null, null, null);
        QuoteDTO q2 = new QuoteDTO(null, null,
            new BaseEntity.WithDetail<>(new StoreInfo(3L, "Hello", "Nil", null)),
            null, null, null);

        String json = om.writeValueAsString(q1);
        String json2 = om.writeValueAsString(q2);
        assertEquals("{\"id\":null,\"productInfo\":null,\"storeInfo\":{\"id\":3},\"quoteDate\":null,\"price\":null,\"discountType\":null}", json);
        assertEquals("{\"id\":null,\"productInfo\":null,\"storeInfo\":{\"id\":3,\"name\":\"Hello\",\"address\":\"Nil\"},\"quoteDate\":null,\"price\":null,\"discountType\":null}", json2);

    }

    @Test
    void testQuoteJSONConvert() throws IOException {
        String json = "{\"storeInfo\": {\"name\": \"name\",\"address\": \"address\",\"url\": \"url\"}}";
        var q1 = om.readValue(json, QuoteDTO.class);
        //assertInstanceOf(BaseEntity.WithDetail.class, a.getStoreInfo());
        if (q1.getStoreInfo() instanceof BaseEntity.WithDetail<StoreInfo>(StoreInfo storeInfo)) {
            assertEquals("name", storeInfo.name());
            assertEquals("address", storeInfo.address());
            assertEquals("url", storeInfo.url());
        } else {
            fail();
        }
        String json2 = "{\"storeInfo\": {\"id\": 3}}";
        var q2 = om.readValue(json2, QuoteDTO.class);
        if (q2.getStoreInfo() instanceof BaseEntity.WithId<StoreInfo>(Long id)) {
            assertEquals(3, id);
        } else {
            fail();
        }
    }
}