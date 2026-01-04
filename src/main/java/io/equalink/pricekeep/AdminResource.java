package io.equalink.pricekeep;

import io.equalink.pricekeep.batch.BatchController;
import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.data.ProductQuoteImportBatch;
import io.equalink.pricekeep.data.StoreImportBatch;
import io.equalink.pricekeep.repo.BatchRepo;
import io.equalink.pricekeep.service.dto.BatchMapper;
import io.equalink.pricekeep.service.dto.JobInfo;
import io.equalink.pricekeep.service.dto.ProductQuoteImportBatchDTO;
import io.equalink.pricekeep.service.dto.StoreImportBatchDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;
import lombok.extern.jbosslog.JBossLog;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.quartz.SchedulerException;

import java.util.List;
import java.util.Optional;

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

    @POST
    @Path("/alert/new")
    public void createAlert() {

    }

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
    public void createBatch(ProductQuoteImportBatchDTO batch) {
        batchRepo.persist(batchMapper.toProductQuoteImportBatchEntity(batch));
    }


    @POST
    @Path("/batch/run/{batchId}")
    public Response runBatch(@PathParam("batchId") Long batchId) {
        var batch = batchRepo.findById(batchId);
        if (batch.isPresent()) {
            try {
                batchController.forceStart(batch.get());
            } catch (SchedulerException e) {
                log.errorv("Scheduler exception: {0}", e.getMessage());
                return Response.serverError().build();
            }
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/batch/all")
    public List<JobInfo> getAllJobs() {
        return batchController.getAllJobs();
    }
}
