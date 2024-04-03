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

@DisplayName("Parameters Integration tests")
public class TestParameters extends AbstractRestTest {
    static JsonObject resourceCenter, flexoProject, vm, behaviour, parameter, concept, flexoEnum;
    static String newSignature;

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
    public void initBhv(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String vmId         = vm.getString("id");
        String name         = "behaviour";
                
        form.set("name", name);
        form.set("is_abstract", "false");
        form.set("visibility", "public");
        form.set("type", "creation");

        client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/")
            .sendForm(form)
            .onSuccess(res -> {
                behaviour = res.bodyAsJsonObject();

                Assertions.assertEquals(behaviour.getString("visibility"), "Public");
                Assertions.assertEquals(behaviour.getString("is_abstract"), "false");
                Assertions.assertEquals(behaviour.getString("name"), name);
                Assertions.assertEquals(behaviour.getString("virtual_model_id"), vmId);
                Assertions.assertEquals(res.statusCode(), 200);

                newSignature = behaviour.getString("signature");
                context.completeNow();
            });
    }

    @Test
    @Order(5)
    @Timeout(1000)
    public void initCp(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String vmId         = vm.getString("id");
        String name         = "Concept";

        form.set("name", name);
        form.set("is_abstract", "false");
        form.set("visibility", "public");

        client.post(9090, "localhost", "/vm/" + vmId + "/cp/")
            .sendForm(form)
            .onSuccess(res -> {
                concept = res.bodyAsJsonObject();

                Assertions.assertEquals(concept.getString("visibility"), "Public");
                Assertions.assertEquals(concept.getString("is_abstract"), "false");
                Assertions.assertEquals(concept.getString("name"), name);
                Assertions.assertEquals(concept.getString("virtual_model_id"), vmId);
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @Test
    @Order(6)
    @Timeout(1000)
    public void initEnum(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String vmId         = vm.getString("id");
        String name         = "MyEnum";

        form.set("name", name);
        form.set("is_abstract", "false");
        form.set("visibility", "default");

        client.post(9090, "localhost", "/vm/" + vmId + "/enums/")
            .sendForm(form)
            .onSuccess(res -> {
                flexoEnum = res.bodyAsJsonObject();

                Assertions.assertEquals(flexoEnum.getString("name"), name);
                Assertions.assertEquals(flexoEnum.getString("is_abstract"), "false");
                Assertions.assertEquals(flexoEnum.getString("visibility"), "Default");
                Assertions.assertEquals(flexoEnum.getString("virtual_model_id"), vmId);
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @RepeatedTest(5)
    @Order(7)
    @Timeout(1000)
    public void createPrimitiveParam(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String vmId         = vm.getString("id");
        String name         = "param" + UUID.randomUUID().hashCode();

        form.set("name", name);
        form.set("is_required", "false");
        form.set("type", "String");

        client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/" + newSignature + "/param/add-primitive")
            .sendForm(form)
            .onSuccess(res -> {
                parameter = res.bodyAsJsonObject();

                Assertions.assertEquals(parameter.getString("type"), "java.lang.String");
                Assertions.assertEquals(parameter.getString("is_required"), "false");
                Assertions.assertEquals(parameter.getString("name"), name);
                Assertions.assertEquals(res.statusCode(), 200);
                newSignature = parameter.getString("behaviour_signature");
                context.completeNow();
            });
    }

    @RepeatedTest(5)
    @Order(8)
    @Timeout(1000)
    public void createFMLInstanceParam(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String vmId         = vm.getString("id");
        String name         = "param" + UUID.randomUUID().hashCode();

        form.set("name", name);
        form.set("is_required", "false");
        form.set("fml_instance_id", concept.getString("id"));

        client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/" + newSignature + "/param/add-fmli")
            .sendForm(form)
            .onSuccess(res -> {
                parameter = res.bodyAsJsonObject();

                Assertions.assertEquals(parameter.getString("type"), "Concept");
                Assertions.assertEquals(parameter.getString("is_required"), "false");
                Assertions.assertEquals(parameter.getString("name"), name);
                Assertions.assertEquals(res.statusCode(), 200);

                newSignature = parameter.getString("behaviour_signature");

                context.completeNow();
            });
    }

    @RepeatedTest(5)
    @Order(9)
    @Timeout(1000)
    public void createFMLEnumParam(Vertx vertx, VertxTestContext context) {
        WebClient client    = WebClient.create(vertx);
        MultiMap form       = MultiMap.caseInsensitiveMultiMap();
        String vmId         = vm.getString("id");
        String name         = "param" + UUID.randomUUID().hashCode();

        form.set("name", name);
        form.set("is_required", "false");
        form.set("enum_id", flexoEnum.getString("id"));

        client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/" + newSignature + "/param/add-fmle")
            .sendForm(form)
            .onSuccess(res -> {
                parameter       = res.bodyAsJsonObject();
                newSignature    = parameter.getString("behaviour_signature");

                System.out.println(res.statusCode());
                System.out.println(res.bodyAsJsonObject());

                Assertions.assertEquals(parameter.getString("type"), "MyEnum");
                Assertions.assertEquals(parameter.getString("is_required"), "false");
                Assertions.assertEquals(parameter.getString("name"), name);
                Assertions.assertEquals(res.statusCode(), 200);

                context.completeNow();
            });
    }

    @Test
    @Order(10)
    @Timeout(1000)
    public void list(Vertx vertx, VertxTestContext context) {
        HttpClient client   = vertx.createHttpClient();
        String vmId         = vm.getString("id");

        client.request(HttpMethod.GET, 9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/" + newSignature + "/param/")
            .compose(req -> req.send()
            .compose(HttpClientResponse::body))
            .onSuccess(res -> {
                Assertions.assertEquals(res.toJsonArray().size(), 15);

                context.completeNow();
            });
    }
}
