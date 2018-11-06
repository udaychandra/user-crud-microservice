package ud.api.services;

import static io.helidon.common.http.Http.Status.*;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A sample user CRUD service that uses an in-memory JSON map.
 */
public class UserService implements Service {

    private final Map<String, JsonObject> users;

    public UserService() {
        this.users = new HashMap<>();
    }

    @Override
    public void update(Routing.Rules rules) {
        rules
                .get("/users", this::listHandler)
                .get("/users/{id}", this::getHandler)
                .post("/users", this::createHandler)
                .put("/users/{id}", this::updateHandler)
                .delete("/users/{id}", this::deleteHandler);
    }

    // Using synchronized for quick demo purposes.
    private synchronized void listHandler(ServerRequest req, ServerResponse res) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        users.values().forEach(builder::add);

        JsonObject result = Json.createObjectBuilder()
                .add("items", builder.build())
                .build();

        res.status(200).send(result);
    }

    private synchronized void getHandler(ServerRequest req, ServerResponse res) {
        JsonObject user = users.get(getID(req));

        if (user != null) {
            res.status(OK_200).send(user);
        } else {
            res.status(NOT_FOUND_404).send();
        }
    }

    private synchronized void createHandler(ServerRequest req, ServerResponse res) {
        req.content()
                .as(JsonObject.class)
                .thenAccept(payload -> {
                    // That's right, no validation for this sample service.
                    String id = generateID();
                    JsonObject user = Json.createObjectBuilder(payload)
                            .add("id", id)
                            .build();

                    users.put(id, user);
                    res.status(CREATED_201).send(user);
                });
    }

    private synchronized void updateHandler(ServerRequest req, ServerResponse res) {
        req.content()
                .as(JsonObject.class)
                .thenAccept(payload -> {
                    String id = getID(req);
                    // Make sure the ID doesn't change.
                    JsonObject user = Json.createObjectBuilder(payload)
                            .add("id", id)
                            .build();

                    users.put(id, user);
                    res.status(ACCEPTED_202).send(user);
                });
    }

    private synchronized void deleteHandler(ServerRequest req, ServerResponse res) {
        JsonObject user = users.remove(getID(req));

        if (user != null) {
            res.status(ACCEPTED_202).send(user);
        } else {
            res.status(NOT_FOUND_404).send();
        }
    }

    private static String getID(ServerRequest req) {
        return req.path().param("id");
    }

    private static String generateID() {
        return UUID.randomUUID().toString();
    }
}
