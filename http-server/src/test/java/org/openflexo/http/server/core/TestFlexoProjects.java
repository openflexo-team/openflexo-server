package org.openflexo.http.server.core;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.Random;
import java.util.UUID;

@DisplayName("Projects Integration tests")
public class TestFlexoProjects extends AbstractRestTest {
    static JsonObject resourceCenter, flexoProject;

    @Test
    @Order(1)
    @Timeout(1000)
    public void init(Vertx vertx, VertxTestContext context){
        WebClient client        = WebClient.create(vertx);
        MultiMap form           = MultiMap.caseInsensitiveMultiMap();

        form.set("rc_path", "resource_center/");

        client.post(9090, "localhost", "/rc/")
            .sendForm(form)
            .onSuccess(res -> {
                resourceCenter = res.bodyAsJsonObject();

                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @RepeatedTest(5)
    @Order(2)
    @Timeout(1000)
    public void createProject(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String name         = "Project" + new Random().nextInt(10);

        form.set("resource_center_id", resourceCenter.getString("id"));
        form.set("name", name);

        client.post(9090, "localhost", "/prj/")
            .sendForm(form)
            .onSuccess(res -> {
                flexoProject = res.bodyAsJsonObject();

                Assertions.assertEquals(flexoProject.getString("name"), name);
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @Test
    @Order(3)
    @Timeout(1000)
    public void getProject(Vertx vertx, VertxTestContext context) {
        HttpClient client   = vertx.createHttpClient();
        String id           = flexoProject.getString("id");

        client.request(HttpMethod.GET, 9090, "localhost", "/prj/" + id)
            .compose(req -> req.send()
            .compose(HttpClientResponse::body))
            .onSuccess(res -> {

                Assertions.assertEquals(res.toJsonObject().getString("id"), id);

                context.completeNow();
            });
    }

    @Test
    @Order(4)
    @Timeout(1000)
    public void listProjects(Vertx vertx, VertxTestContext context) {
        HttpClient client = vertx.createHttpClient();

        client.request(HttpMethod.GET, 9090, "localhost", "/prj/")
            .compose(req -> req.send()
            .compose(HttpClientResponse::body))
            .onSuccess(res -> {
                Assertions.assertEquals(res.toJsonArray().size(), 5);

                context.completeNow();
            });
        Assertions.assertEquals(3, 3);
    }

    @RepeatedTest(5)
    @Order(5)
    @Timeout(1000)
    public void createFolder(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String id           = flexoProject.getString("id");
        String name         = "Folder" + UUID.randomUUID().hashCode();

        form.set("name", name);

        client.post(9090, "localhost", "/prj/" + id + "/fdr/")
            .sendForm(form)
            .onSuccess(res -> {
                Assertions.assertEquals(res.bodyAsJsonObject().getString("name"), name);
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @Test
    @Order(6)
    @Timeout(1000)
    public void listFolders(Vertx vertx, VertxTestContext context) {
        HttpClient client   = vertx.createHttpClient();
        String id           = flexoProject.getString("id");

        client.request(HttpMethod.GET, 9090, "localhost", "/prj/" + id + "/fdr/")
            .compose(req -> req.send()
            .compose(HttpClientResponse::body))
            .onSuccess(res -> {
                Assertions.assertEquals(res.toJsonArray().size(), 5);

                context.completeNow();
            });
    }
}
