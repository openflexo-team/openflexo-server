package org.openflexo.http.server.core;


import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openflexo.http.server.OpenFlexoServer;

import java.io.File;
import java.io.IOException;

@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractRestTest {
    @BeforeAll
    public static void setUp(Vertx vertx, VertxTestContext testContext) {
        File dir = new File("src/main/resources/resource_center/");
        dir.mkdirs();

        vertx.deployVerticle(new OpenFlexoServer(), testContext.succeedingThenComplete());
    }

    @AfterAll
    public static void cleanUp(Vertx vertx) {

        try {
            FileUtils.deleteDirectory(new File("src/main/resources/resource_center/"));
        } catch (IOException e) {
            ;
        }
        vertx.close();
    }
}
