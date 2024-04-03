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
@DisplayName("Resource Centers Integration tests")
public class TestResourceCenters extends AbstractRestTest {

    static JsonObject resourceCenter, uploadedResourceCenter;

    @Test
    @Order(1)
    @Timeout(1000)
    public void create(Vertx vertx, VertxTestContext context) {
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
    public void list(Vertx vertx, VertxTestContext context) {
        HttpClient client = vertx.createHttpClient();

        client.request(HttpMethod.GET, 9090, "localhost", "/rc/")
            .compose(req -> req.send()
            .compose(HttpClientResponse::body))
            .onSuccess(res -> {
                Assertions.assertEquals(res.toJsonArray().size(), 1);

                context.completeNow();
            });
    }

    @Test
    @Order(3)
    @Timeout(1000)
    public void get(Vertx vertx, VertxTestContext context) {
        HttpClient client   = vertx.createHttpClient();
        String id           = resourceCenter.getString("id");

        client.request(HttpMethod.GET, 9090, "localhost", "/rc/" + id)
                .compose(req -> req.send()
                .compose(HttpClientResponse::body))
                .onSuccess(res -> {

                    Assertions.assertEquals(res.toJsonObject().getString("id"), id);

                    context.completeNow();
                });

    }

//
//    @Test
//    @Order(4)
//    @Timeout(5000)
//    public void upload(Vertx vertx, VertxTestContext context) {
//        WebClient client        = WebClient.create(vertx);
//        File resourcesDirectory = new File("src/test/resources/testTest.prj.zip");
//
//        MultipartForm form = MultipartForm.create()
//                .binaryFileUpload(
//                        "testRest",
//                        "testTest.prj.zip",
//                        resourcesDirectory.getAbsolutePath(),
//                        "application/zip");
//
//        client.post(9090, "localhost", "/rc/upload")
//                .putHeader("Content-type","multipart/form-data")
//                .sendMultipartForm(form)
//                .onSuccess(res -> {
//                    uploadedResourceCenter = res.bodyAsJsonObject();
//
//                    Assertions.assertEquals(res.statusCode(), 200);
//
//                    context.completeNow();
//                });
//    }
}
