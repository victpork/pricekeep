package io.equalink.pricekeep;

import io.equalink.pricekeep.batch.BatchController;
import io.equalink.pricekeep.repo.BatchRepo;
import io.equalink.pricekeep.repo.StoreRepo;
import io.equalink.pricekeep.service.dto.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.quartz.SchedulerException;

import java.util.List;

@JBossLog
@Path("/admin")
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    @Inject
    BatchRepo batchRepo;

    @Inject
    BatchController batchController;

    @Inject
    BatchMapper batchMapper;

    @Inject
    StoreRepo storeRepo;

    @Inject
    StoreMapper storeMapper;

    @POST
    @Path("/store/new")
    public void createStore() {

    }

    @POST
    @Path("/batch/newStoreImport")
    public void createBatch(StoreImportBatchDTO batch) {
        batchRepo.persist(batchMapper.toStoreImportBatchEntity(batch));
    }

    @POST
    @Path("/batch/newProductQuoteImport")
    @APIResponse(
        responseCode = "202",
        description = "Batch created",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(
                implementation = Result.class
            )
        )
    )
    @APIResponse(
        responseCode = "400",
        description = "Bad request",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(
                implementation = Result.class
            )
        )
    )
    public Response createBatch(ProductQuoteImportBatchDTO batch) {
        var batchEntity = batchMapper.toProductQuoteImportBatchEntity(batch);
        batchRepo.persist(batchEntity);
        try {
            batchController.createBatch(batchEntity);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return Response.accepted(new Result("ok", "Batch created")).build();
    }


    @POST
    @Path("/batch/{batchId}/run/")
    public Response runBatch(@PathParam("batchId") Long batchId) {
        var batch = batchRepo.findById(batchId);
        if (batch.isPresent()) {
            try {
                batchController.forceStart(batch.get());
            } catch (SchedulerException e) {
                log.errorv("Scheduler exception: {0}", e.getMessage());
                return Response.serverError().build();
            }
            return Response.ok(Result.builder().result("ok").msg("batch queued to run")).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity(Result.builder().result("error").msg("batch not found")).build();
    }

    @GET
    @Path("/batch/all")
    public List<JobInfo> getAllJobs() {
        return batchController.getAllJobs();
    }

    @GET
    @Path("/store/search")
    public List<StoreInfo> getAllStores(@QueryParam("q") @DefaultValue("") String storeName) {
        log.infov("Searching for stores: {0}", "%" + storeName.replaceAll("%", "").toLowerCase() + "%");
        return storeRepo.findStoreByName("%" + storeName.replaceAll("%", "").toLowerCase() + "%").stream().map(storeMapper::toDTO).toList();
    }

    @DELETE
    @Path("/batch/delete")
    public Response deleteBatch(List<Long> batchId) {
        batchId.stream().map(batchRepo::findById).forEach(b -> b.ifPresent(batchRepo::delete));
        return Response.ok(Result.builder().result("ok").msg("batch deleted")).build();
    }

    @POST
    @Path("/batch/enable")
    public Response enableBatch(List<Long> batchId) {
        batchId.stream().map(batchRepo::findById).forEach(b -> b.ifPresent(bb -> {
            try {
                batchController.enableBatch(bb);
            } catch (SchedulerException _) {
            }
        }));
        return Response.ok(Result.builder().result("ok").msg("batch enabled")).build();
    }

    @POST
    @Path("/batch/disable")
    public Response disableBatch(List<Long> batchId) {
        batchId.stream().map(batchRepo::findById).forEach(b -> b.ifPresent(bb -> {
            try {
                batchController.disableBatch(bb);
            } catch (SchedulerException _) {
            }
        }));
        return Response.ok(Result.builder().result("ok").msg("batch disabled")).build();
    }
}
