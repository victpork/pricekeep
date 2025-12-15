package io.equalink.pricekeep;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class StaticResource {

    @ConfigProperty(name = "fetchImgPath")
    String imgDir;


    void installRoute(@Observes Router router) {
        // Configure the route to serve static files from a specific path
        router.route("/static/assets/img/*")
            .handler(StaticHandler.create(FileSystemAccess.ROOT, imgDir).setCachingEnabled(true)); // The absolute path to your external directory
    }
}
