package io.equalink.pricekeep;

import io.equalink.pricekeep.data.Quote;
import io.equalink.pricekeep.service.PeriodLength;
import io.equalink.pricekeep.service.dto.*;
import io.equalink.pricekeep.service.pricefetch.ProductFetchService;
import io.equalink.pricekeep.service.quote.ProductService;
import io.equalink.pricekeep.service.quote.QuoteService;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Multi;
import jakarta.data.exceptions.DataException;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.logging.Logger;

import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Path("/product")
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {
    @Inject
    QuoteService quoteService;

    @Inject
    ProductService productService;

    @Inject
    ProductMapper pMapper;

    @Inject
    QuoteMapper quoteMapper;

    @Inject
    ProductFetchService fetchService;

    @Context
    UriInfo uriInfo;

    @Inject
    Validator validator;

    private static final Logger LOG = Logger.getLogger(ProductResource.class);

    @GET
    @Path("/search")
    public List<ProductInfo> searchProduct(@QueryParam("q") @NotNull String keyword) {

        return productService.getProductByKeyword(keyword).stream().map(p -> pMapper.toDTO(p)).toList();
    }

    @GET
    @Path("/all")
    public List<ProductInfo> getAllProduct(@QueryParam("page") Integer page) {
        if (page == null) page = 1;
        return productService.getAllProduct(page).stream().map(p -> pMapper.toDTO(p)).toList();
    }

    @GET
    @Path("/gtin/{gtin}")
    public ProductInfo getProductByGtin(@PathParam("gtin") @NotNull String gtin) {
        var product = productService.getProductByGTIN(gtin).stream().findFirst().orElseThrow();
        return pMapper.toDTO(product);
    }

    @GET
    @Path("/{productId}")
    public ProductInfo getProductById(@PathParam("productId") Long productId) {
        var product = productService.getProductById(productId).orElseThrow();
        return pMapper.toDTO(product);
    }

    @GET
    @Path("/suggest")
    public List<String> suggestedWords(@QueryParam("q") @NotNull @Valid String input) {

        return productService.suggestWords(input);
    }

    @POST
    @Path("/new")
    @APIResponse(
        responseCode = "201",
        description = "Successfully created",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(
                ref = "#/components/schemas/ProductInfo"
            )
        )

    )
    @APIResponse(
        responseCode = "400",
        description = "Data conflict",
        content = @Content(mediaType = "application/json")
    )
    public Response createProduct(@Valid ProductInfo product) {

        Set<ConstraintViolation<ProductInfo>> violations = validator.validate(product);
        if (!violations.isEmpty()) {
            violations.forEach(v -> LOG.info(v.getMessage()));
        } else {
            LOG.info("No validation errors");
        }

        var eProduct = pMapper.toEntity(product);
        try {
            productService.persist(eProduct);
        } catch (DataException e) {
            throw new BadRequestException(e);
        }
        return Response.created(UriBuilder.fromResource(ProductResource.class).path("/{id}").build(eProduct.getId())).entity(pMapper.toDTO(eProduct)).build();
    }

    @GET
    @Path("/extSources")
    public List<String> getAvailableExtSources() {
        return fetchService.getListOfAvailableServices();
    }

    @POST
    @NonBlocking
    @Path("/searchExt")
    public Multi<QuoteDTO> findExternalProduct(ExternalProductQueryMessage request) {
        //Context context = Vertx.currentContext();
        AtomicLong idGen = new AtomicLong(1);
        return fetchService.getProductQuoteFromExternalServices(request.keyword(), request.sources())
                   .onItem().transform(pMapper::toQuoteDTO);
    }

    @POST
    @Path("/{productId}/quote")
    public Response quotePrice(@PathParam("productId") Long productId, @Valid QuoteDTO qDTO) {
        Quote q = quoteMapper.toEntity(qDTO);
        //Product p = productService.getProductById(productId).orElseThrow();

        productService.quotePriceForProduct(productId, q);

        return Response.accepted(pMapper.toQuoteDTO(q)).build();
    }

    @GET
    @Path("/{productId}/quoteHist")
    public List<QuoteDTO> getPriceHistory(@PathParam("productId") Long productId, @QueryParam("l") PeriodLength length, @QueryParam("id") boolean includeDiscount) {

        Period p = switch (length) {
            case PeriodLength.WEEK -> Period.ofWeeks(1);
            case PeriodLength.MONTH -> Period.ofMonths(1);
            case PeriodLength.QUARTER -> Period.ofMonths(3);
            case PeriodLength.YEAR -> Period.ofYears(1);
        };
        List<Quote> quotes = productService.getPriceHistory(productId, p, includeDiscount);
        return quotes.stream().map(pMapper::toQuoteDTO).toList();
    }

    @GET
    @Path("/{productId}/chart")
    public List<ChartNode> getChartData(@PathParam("productId") Long productId, @QueryParam("showDiscount") boolean showDiscount, @NotNull @QueryParam("tr") TimeRange timeRange) {
        List<Quote> t = productService.getPriceHistory(productId, timeRange.period, showDiscount);
        return t.stream().map(quoteMapper::toChartNode).toList();
    }

    @GET
    @Path("/alerts")
    public List<QuoteDTO> getTodayAlertTrigger() {
        return List.of();
    }

    @POST
    @Path("/{productId}/edit")
    public Response editProduct(@PathParam("productId") Long productId, @Valid ProductInfo pInfo) {
        var existingProduct = productService.getProductById(productId).orElseThrow();
        existingProduct.setName(pInfo.name());
        existingProduct.setDescription(pInfo.desc());
        existingProduct.setGtin(pInfo.gtin());
        //existingProduct.setImageUrl(pInfo.imageUrl());
        productService.updateProductInfo(existingProduct);

        return Response.ok(pMapper.toDTO(existingProduct)).build();
    }
}
