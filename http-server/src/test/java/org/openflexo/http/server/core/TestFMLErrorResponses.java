package org.openflexo.http.server.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openflexo.foundation.fml.rt.error.FMLInternalError;
import org.openflexo.foundation.fml.rt.error.FMLNullReferenceError;
import org.openflexo.http.server.core.serializers.JsonSerializer;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;

/**
 * The answers of the server to a failure of FML (CORE-F-11 phase 5): JSON {kind, key, message, location, stack}, 400 for a foreseen
 * error, 500 for a defect; a failed behaviour saves nothing; the terminal answers the same JSON, and 422 for an invalid command
 */
@DisplayName("FML error responses")
public class TestFMLErrorResponses extends AbstractRestTest {

	private static final String FAILING_EXPRESSION = "\"abc\".substring(1,42)";

	static JsonObject resourceCenter, flexoProject, vm, behaviour, instance;
	static String sessionId;

	@Test
	@Order(1)
	@Timeout(1000)
	public void initRc(Vertx vertx, VertxTestContext context) {
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		form.set("rc_path", "resource_center/");
		WebClient.create(vertx).post(9090, "localhost", "/rc/").sendForm(form).onSuccess(res -> context.verify(() -> {
			resourceCenter = res.bodyAsJsonObject();
			Assertions.assertEquals(200, res.statusCode());
			context.completeNow();
		})).onFailure(context::failNow);
	}

	@Test
	@Order(2)
	@Timeout(1000)
	public void initPrj(Vertx vertx, VertxTestContext context) {
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		form.set("resource_center_id", resourceCenter.getString("id"));
		form.set("name", "ErrorProject");
		WebClient.create(vertx).post(9090, "localhost", "/prj/").sendForm(form).onSuccess(res -> context.verify(() -> {
			flexoProject = res.bodyAsJsonObject();
			Assertions.assertEquals(200, res.statusCode());
			context.completeNow();
		})).onFailure(context::failNow);
	}

	@Test
	@Order(3)
	@Timeout(1000)
	public void initVm(Vertx vertx, VertxTestContext context) {
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		form.set("project_id", flexoProject.getString("id"));
		form.set("name", "FailingModel");
		form.set("is_abstract", "false");
		form.set("visibility", "public");
		WebClient.create(vertx).post(9090, "localhost", "/vm/").sendForm(form).onSuccess(res -> context.verify(() -> {
			vm = res.bodyAsJsonObject();
			Assertions.assertEquals(200, res.statusCode());
			context.completeNow();
		})).onFailure(context::failNow);
	}

	@Test
	@Order(4)
	@Timeout(1000)
	public void initFailingBehaviour(Vertx vertx, VertxTestContext context) {
		String vmId = vm.getString("id");
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		form.set("name", "fail");
		form.set("is_abstract", "false");
		form.set("visibility", "public");
		form.set("type", "action");
		WebClient.create(vertx).post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/").sendForm(form)
				.onSuccess(res -> context.verify(() -> {
					behaviour = res.bodyAsJsonObject();
					Assertions.assertEquals(200, res.statusCode());
					context.completeNow();
				})).onFailure(context::failNow);
	}

	/** The behaviour logs an expression that does not resolve when it runs: a foreseen FMLResolutionError */
	@Test
	@Order(5)
	@Timeout(1000)
	public void addFailingStatement(Vertx vertx, VertxTestContext context) {
		String vmId = vm.getString("id");
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		form.set("value", FAILING_EXPRESSION);
		form.set("level", "info");
		WebClient.create(vertx).post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/" + behaviour.getString("signature") + "/act/add-log")
				.sendForm(form).onSuccess(res -> context.verify(() -> {
					Assertions.assertEquals(200, res.statusCode(), res.bodyAsString());
					context.completeNow();
				})).onFailure(context::failNow);
	}

	@Test
	@Order(6)
	@Timeout(1000)
	public void createInstance(Vertx vertx, VertxTestContext context) {
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		form.set("title", "title" + UUID.randomUUID().hashCode());
		form.set("name", "failing" + Math.abs(UUID.randomUUID().hashCode()));
		WebClient.create(vertx).post(9090, "localhost", "/prj/" + flexoProject.getString("id") + "/vm/" + vm.getString("id") + "/instances/")
				.sendForm(form).onSuccess(res -> context.verify(() -> {
					Assertions.assertEquals(200, res.statusCode(), res.bodyAsString());
					instance = res.bodyAsJsonObject();
					context.completeNow();
				})).onFailure(context::failNow);
	}

	/** A failing behaviour answers 400 with its FML error, located in the behaviour, and its instance is not saved */
	@Test
	@Order(7)
	@Timeout(1000)
	public void executeFailingBehaviour(Vertx vertx, VertxTestContext context) throws IOException {
		long before = lastModifiedOfInstance(instance.getString("name"));
		String url = "/prj/" + flexoProject.getString("id") + "/vm/" + vm.getString("id") + "/instances/" + instance.getString("id") + "/bhv/"
				+ behaviour.getString("signature") + "/exe";
		WebClient.create(vertx).post(9090, "localhost", url).sendForm(MultiMap.caseInsensitiveMultiMap())
				.onSuccess(res -> context.verify(() -> {
					Assertions.assertEquals(400, res.statusCode(), res.bodyAsString());
					JsonObject error = res.bodyAsJsonObject();
					// A method call on a literal does not resolve: a foreseen resolution error, raised when the statement runs
					Assertions.assertEquals("Resolution error", error.getString("kind"));
					Assertions.assertEquals("fml_resolution_error", error.getString("key"));
					Assertions.assertEquals("FMLError", error.getString("resource_type"));
					Assertions.assertNotNull(error.getString("message"));
					JsonArray stack = error.getJsonArray("stack");
					Assertions.assertFalse(stack.isEmpty(), error.encodePrettily());
					Assertions.assertEquals(stack.getJsonObject(0), error.getJsonObject("location"));
					boolean behaviourFrame = false;
					for (int i = 0; i < stack.size(); i++) {
						if (behaviour.getString("signature").equals(stack.getJsonObject(i).getString("behaviour"))) {
							behaviourFrame = true;
						}
					}
					Assertions.assertTrue(behaviourFrame, error.encodePrettily());
					Assertions.assertEquals(before, lastModifiedOfInstance(instance.getString("name")), "The failed instance was saved");
					context.completeNow();
				})).onFailure(context::failNow);
	}

	@Test
	@Order(10)
	@Timeout(1000)
	public void initTerminal(Vertx vertx, VertxTestContext context) {
		WebClient.create(vertx).post(9090, "localhost", "/terminal/init").sendForm(MultiMap.caseInsensitiveMultiMap())
				.onSuccess(res -> context.verify(() -> {
					Assertions.assertEquals(200, res.statusCode(), res.bodyAsString());
					sessionId = res.bodyAsJsonObject().getString("session_id");
					Assertions.assertNotNull(sessionId);
					context.completeNow();
				})).onFailure(context::failNow);
	}

	@Test
	@Order(11)
	@Timeout(1000)
	public void terminalAssignment(Vertx vertx, VertxTestContext context) {
		execute(vertx, "s = \"abc\"").onSuccess(res -> context.verify(() -> {
			Assertions.assertEquals(200, res.statusCode(), res.bodyAsString());
			context.completeNow();
		})).onFailure(context::failNow);
	}

	@Test
	@Order(12)
	@Timeout(1000)
	public void terminalFMLError(Vertx vertx, VertxTestContext context) {
		execute(vertx, "s.substring(1,42)").onSuccess(res -> context.verify(() -> {
			Assertions.assertEquals(400, res.statusCode(), res.bodyAsString());
			JsonObject error = res.bodyAsJsonObject();
			Assertions.assertEquals("fml_host_error", error.getString("key"));
			Assertions.assertEquals("Failure of Java code", error.getString("kind"));
			context.completeNow();
		})).onFailure(context::failNow);
	}

	@Test
	@Order(13)
	@Timeout(1000)
	public void terminalFailedAssertion(Vertx vertx, VertxTestContext context) {
		execute(vertx, "assert false").onSuccess(res -> context.verify(() -> {
			Assertions.assertEquals(400, res.statusCode(), res.bodyAsString());
			JsonObject error = res.bodyAsJsonObject();
			Assertions.assertEquals("assert failed: false", error.getString("message"));
			context.completeNow();
		})).onFailure(context::failNow);
	}

	@Test
	@Order(14)
	@Timeout(1000)
	public void terminalInvalidCommand(Vertx vertx, VertxTestContext context) {
		execute(vertx, "unknownVariable.foo()").onSuccess(res -> context.verify(() -> {
			Assertions.assertEquals(422, res.statusCode(), res.bodyAsString());
			context.completeNow();
		})).onFailure(context::failNow);
	}

	/** The JSON of a defect of the platform: its kind is an internal error, whatever it wraps */
	@Test
	@Order(20)
	public void serializeInternalError() {
		JsonObject error = JsonSerializer.fmlErrorSerializer(new FMLInternalError(new IllegalStateException("broken")));
		Assertions.assertEquals("fml_internal_error", error.getString("key"));
		Assertions.assertEquals("Internal error", error.getString("kind"));
		Assertions.assertTrue(error.getString("message").contains("broken"), error.encodePrettily());
		Assertions.assertTrue(error.getJsonArray("stack").isEmpty());
		Assertions.assertNull(error.getValue("location"));
	}

	/** The JSON of a foreseen error with a frame */
	@Test
	@Order(21)
	public void serializeForeseenError() {
		FMLNullReferenceError failure = new FMLNullReferenceError("x is null");
		failure.getFMLStack().pushFrame("doIt()", "Concept", null);
		JsonObject error = JsonSerializer.fmlErrorSerializer(failure);
		Assertions.assertEquals("fml_null_reference_error", error.getString("key"));
		Assertions.assertEquals("Null reference", error.getString("kind"));
		Assertions.assertEquals("x is null", error.getString("message"));
		Assertions.assertEquals("doIt()", error.getJsonObject("location").getString("behaviour"));
		Assertions.assertEquals("Concept", error.getJsonObject("location").getString("concept"));
	}

	private static io.vertx.core.Future<io.vertx.ext.web.client.HttpResponse<io.vertx.core.buffer.Buffer>> execute(Vertx vertx,
			String command) {
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		form.set("session_id", sessionId);
		form.set("command", command);
		return WebClient.create(vertx).post(9090, "localhost", "/terminal/execute").sendForm(form);
	}

	/** The latest modification of the files of the instance named so, 0 when it has none (never saved) */
	private static long lastModifiedOfInstance(String name) throws IOException {
		File rc = new File("src/main/resources/resource_center/");
		if (!rc.exists()) {
			return 0;
		}
		try (Stream<Path> paths = Files.walk(rc.toPath())) {
			return paths.filter(p -> p.toString().contains(name)).map(Path::toFile).filter(File::isFile).mapToLong(File::lastModified).max()
					.orElse(0);
		}
	}
}
