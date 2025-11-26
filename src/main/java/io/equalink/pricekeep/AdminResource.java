package io.equalink.pricekeep;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@ApplicationPath("/admin")
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    @POST
    @Path("/alert/new")
    public void createAlert() {

    }

    @POST
    @Path("/store/new")
    public void createStore() {

    }
}
