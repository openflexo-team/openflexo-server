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

@DisplayName("Properties Integration tests")
public class TestProperties extends AbstractRestTest {
    static JsonObject resourceCenter, flexoProject, vm, prop, primitiveProp;

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
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();

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
        form.set("name", "VirtualModel1");
        form.set("is_abstract", "false");
        form.set("visibility", "public");

        client.post(9090, "localhost", "/vm/")
            .sendForm(form)
            .onSuccess(res -> {
                vm = res.bodyAsJsonObject();

                Assertions.assertEquals(vm.getString("name"), "VirtualModel1");
                Assertions.assertEquals(flexoProject.getString("project_id"), vm.getString("project_id"));
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @RepeatedTest(5)
    @Order(4)
    @Timeout(1000)
    public void createPrimitive(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String vmId         = vm.getString("id");
        String name         = "prop" + UUID.randomUUID().hashCode();


        form.set("name", name);
        form.set("type", "String");
        form.set("cardinality", "One");

        client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/prp/add-primitive")
            .sendForm(form)
            .onSuccess(res -> {
                primitiveProp = res.bodyAsJsonObject();

                Assertions.assertEquals(primitiveProp.getString("type"), "String");
                Assertions.assertEquals(primitiveProp.getString("cardinality"), "One");
                Assertions.assertEquals(primitiveProp.getString("name"), name);
                Assertions.assertEquals(primitiveProp.getString("description"), "description");
                Assertions.assertEquals(primitiveProp.getString("virtual_model_id"), vmId);
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @RepeatedTest(5)
    @Order(5)
    @Timeout(1000)
    public void createInstanceRole(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String vmId         = vm.getString("id");
        String name         = "instanceRole" + UUID.randomUUID().hashCode();

        form.set("name", name);
        form.set("concept_id", vmId);
        form.set("cardinality", "One");

        client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/prp/add-fcir")
            .sendForm(form)
            .onSuccess(res -> {
                prop = res.bodyAsJsonObject();

                Assertions.assertEquals(prop.getString("concept_id"), vmId);
                Assertions.assertEquals(prop.getString("description"), "description");
                Assertions.assertEquals(prop.getString("cardinality"), "One");
                Assertions.assertEquals(prop.getString("name"), name);
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @RepeatedTest(5)
    @Order(6)
    @Timeout(1000)
    public void createModelSlot(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String vmId         = vm.getString("id");
        String name         = "modelSlot" + UUID.randomUUID().hashCode();

        form.set("name", name);
        form.set("concept_id", vmId);
        form.set("required", "true");
        form.set("read_only", "false");
        form.set("technology_adapter", "fmlrt");
        form.set("virtual_model_id", vmId);

        client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/prp/add-ms")
            .sendForm(form)
            .onSuccess(res -> {
                prop = res.bodyAsJsonObject();

                Assertions.assertEquals(prop.getString("required"), "true");
                Assertions.assertEquals(prop.getString("read_only"), "false");
                Assertions.assertEquals(prop.getString("technology_adapter"), "FML technology adapter");
                Assertions.assertEquals(prop.getString("virtual_model_id"), vmId);
                Assertions.assertEquals(prop.getString("description"), "description");
                Assertions.assertEquals(prop.getString("name"), name);
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @Test
    @Order(7)
    @Timeout(1000)
    public void get(Vertx vertx, VertxTestContext context) {
        HttpClient client   = vertx.createHttpClient();
        String name         = primitiveProp.getString("name");
        String vmId         = vm.getString("id");

        client.request(HttpMethod.GET, 9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/prp/" + name)
            .compose(req -> req.send()
            .compose(HttpClientResponse::body))
            .onSuccess(res -> {

                Assertions.assertEquals(res.toJsonObject().getString("cardinality"), "One");
                Assertions.assertEquals(res.toJsonObject().getString("name"), name);
                Assertions.assertEquals(res.toJsonObject().getString("type"), "String");
                Assertions.assertEquals(res.toJsonObject().getString("description"), "description");

                context.completeNow();
            });
    }

    @Test
    @Order(8)
    @Timeout(1000)
    public void list(Vertx vertx, VertxTestContext context) {
        HttpClient client   = vertx.createHttpClient();
        String vmId         = vm.getString("id");

        client.request(HttpMethod.GET, 9090,"localhost", "/vm/" + vmId + "/cp/" + vmId + "/prp/")
            .compose(req -> req.send()
            .compose(HttpClientResponse::body))
            .onSuccess(res -> {

                Assertions.assertEquals(res.toJsonArray().size(), 15);

                context.completeNow();
            });

    }
}
