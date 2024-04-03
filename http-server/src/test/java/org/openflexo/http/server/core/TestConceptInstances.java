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
import java.util.UUID;

@DisplayName("Virtual Model Instances Integration tests")
public class TestConceptInstances extends AbstractRestTest {
    static JsonObject resourceCenter, flexoProject, vm, concept, vmi, cpi;

    @Test
    @Order(1)
    @Timeout(1000)
    public void initRc(Vertx vertx, VertxTestContext context){
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

    @Test
    @Order(2)
    @Timeout(1000)
    public void initPrj(Vertx vertx, VertxTestContext context){
        WebClient client        = WebClient.create(vertx);
        MultiMap form           = MultiMap.caseInsensitiveMultiMap();

        form.set("resource_center_id", resourceCenter.getString("id"));
        form.set("name", "TestProject");

        client.post(9090, "localhost", "/prj/")
            .sendForm(form)
            .onSuccess(res -> {
                flexoProject = res.bodyAsJsonObject();

                Assertions.assertEquals(flexoProject.getString("name"), "TestProject");
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @Test
    @Order(3)
    @Timeout(1000)
    public void initVm(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();

        form.set("project_id", flexoProject.getString("id"));
        form.set("name", "VirtualModel");
        form.set("is_abstract", "false");
        form.set("visibility", "public");

        client.post(9090, "localhost", "/vm/")
            .sendForm(form)
            .onSuccess(res -> {
                vm = res.bodyAsJsonObject();

                Assertions.assertEquals(vm.getString("name"), "VirtualModel");
                Assertions.assertEquals(flexoProject.getString("project_id"), vm.getString("project_id"));
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @Test
    @Order(4)
    @Timeout(1000)
    public void initCp(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String vmId         = vm.getString("id");

        form.set("name", "Concept");
        form.set("is_abstract", "false");
        form.set("visibility", "public");

        client.post(9090, "localhost", "/vm/" + vmId + "/cp/")
            .sendForm(form)
            .onSuccess(res -> {
                concept = res.bodyAsJsonObject();

                Assertions.assertEquals(concept.getString("visibility"), "Public");
                Assertions.assertEquals(concept.getString("is_abstract"), "false");
                Assertions.assertEquals(concept.getString("name"), "Concept");
                Assertions.assertEquals(concept.getString("virtual_model_id"), vmId);
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @Test
    @Order(5)
    @Timeout(1000)
    public void initVmi(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String name         = "instance" + UUID.randomUUID().hashCode();
        String title        = "title" + UUID.randomUUID().hashCode();

        form.set("title", title);
        form.set("name", name);

        client.post(9090, "localhost", "/prj/" + flexoProject.getString("id") + "/vm/" + vm.getString("id") + "/instances/")
            .sendForm(form)
            .onSuccess(res -> {
                vmi = res.bodyAsJsonObject();

                Assertions.assertEquals(vmi.getString("name"), name);
                Assertions.assertEquals(vmi.getString("title"), title);
                Assertions.assertEquals(vmi.getString("virtual_model_id"), vm.getString("id"));
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @RepeatedTest(5)
    @Order(6)
    @Timeout(1000)
    public void create(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();

        form.set("concept_id", concept.getString("id"));
        form.set("project_id", flexoProject.getString("id"));
        form.set("container_id", vmi.getString("id"));

        client.post(9090, "localhost", "/cpi/")
            .sendForm(form)
            .onSuccess(res -> {
                cpi = res.bodyAsJsonObject();

                Assertions.assertEquals(cpi.getString("concept_id"), concept.getString("id"));
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @Test
    @Order(7)
    @Timeout(1000)
    public void get(Vertx vertx, VertxTestContext context) {
        HttpClient client   = vertx.createHttpClient();
        String id           = cpi.getString("id");

        client.request(HttpMethod.GET, 9090, "localhost", "/cpi/" + cpi.getString("id"))
            .compose(req -> req.send()
            .compose(HttpClientResponse::body))
            .onSuccess(res -> {
                System.out.println(res.toJson().toString());
                Assertions.assertEquals(cpi.getString("concept_id"), res.toJsonObject().getString("id"));
                Assertions.assertEquals(res.toJsonObject().getString("id"), id);

                context.completeNow();
            });
    }

    @Test
    @Order(8)
    @Timeout(1000)
    public void list(Vertx vertx, VertxTestContext context) {
        HttpClient client   = vertx.createHttpClient();

        client.request(HttpMethod.GET, 9090, "localhost", "/cpi/")
            .compose(req -> req.send()
            .compose(HttpClientResponse::body))
            .onSuccess(res -> {
                System.out.println(res.toJson().toString());
                Assertions.assertEquals(res.toJsonArray().size(), 5);

                context.completeNow();
            });
    }
}
