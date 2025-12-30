package io.equalink.pricekeep;

import io.equalink.pricekeep.batch.BatchController;
import io.equalink.pricekeep.data.BaseBatch;
import io.equalink.pricekeep.repo.BatchRepo;
import io.equalink.pricekeep.service.dto.JobInfo;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;
import lombok.extern.jbosslog.JBossLog;
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


    @POST
    @Path("/alert/new")
    public void createAlert() {

    }

    @POST
    @Path("/store/new")
    public void createStore() {

    }

    @POST
    @Path("/batch/new")
    public void createDelete() {
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
