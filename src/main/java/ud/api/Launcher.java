package ud.api;

import static io.helidon.common.http.Http.Status.NOT_FOUND_404;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.json.JsonSupport;
import ud.api.services.UserService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Launcher {

    private static final Logger logger = Logger.getLogger(Launcher.class.getName());

    /**
     * Start the CRUD service using Helidon reactive web server
     */
    public static void main(String[] args) {
        ServerConfiguration config = ServerConfiguration
                .builder()
                .port(8080)
                .build();

        WebServer
                .create(config, createServicesRouting())
                .start()
                .thenAccept(ws -> logger.info("Service running at: http://localhost:" + ws.port()));
    }

    private static Routing createServicesRouting() {
        return Routing.builder()
                // Add JSON support to all end-points.
                .register(JsonSupport.get())
                .register("/api", new UserService())
                // Simple exception handler.
                .error(Exception.class, (req, res, ex) -> {
                    // We are masking internal errors.
                    res.status(NOT_FOUND_404);
                    res.send("Unable to parse request");
                    logger.log(Level.SEVERE, "Unable to parse request", ex);
                })
                .build();
    }
}
