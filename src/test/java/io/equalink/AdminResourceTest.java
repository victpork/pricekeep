package io.equalink;


import io.equalink.pricekeep.AdminResource;
import io.equalink.pricekeep.repo.StoreRepo;
import io.equalink.pricekeep.service.dto.StoreInfo;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestHTTPEndpoint(AdminResource.class)
public class AdminResourceTest {

    @Inject
    StoreRepo storeRepo;

    @Test
    void testCreateStore() {
        StoreInfo storeInfo = StoreInfo.builder().name("Test Store").build();
        with().contentType(ContentType.JSON).body(storeInfo).when().post("/store/new").then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .body("entity.id", notNullValue(),
                "entity.name", equalTo("Test Store"));
    }
}
