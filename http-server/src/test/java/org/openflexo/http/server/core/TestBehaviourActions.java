package org.openflexo.http.server.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;

@DisplayName("Behaviour Actions Integration tests")
public class TestBehaviourActions extends AbstractRestTest {
	static JsonObject resourceCenter, flexoProject, vm, behaviour, action, prop, param;
	static String newSignature;

	@Test
	@Order(1)
	@Timeout(1000)
	public void initRc(Vertx vertx, VertxTestContext context) {
		WebClient client = WebClient.create(vertx);
		MultiMap form = MultiMap.caseInsensitiveMultiMap();

		form.set("rc_path", "resource_center/");

		client.post(9090, "localhost", "/rc/").sendForm(form).onSuccess(res -> {
			context.verify(() -> {
				resourceCenter = res.bodyAsJsonObject();
				Assertions.assertEquals(res.statusCode(), 200);
				context.completeNow();
			});
		}).onFailure(context::failNow);
	}

	@Test
	@Order(2)
	@Timeout(1000)
	public void initPrj(Vertx vertx, VertxTestContext context) {
		WebClient client = WebClient.create(vertx);
		MultiMap form = MultiMap.caseInsensitiveMultiMap();

		form.set("resource_center_id", resourceCenter.getString("id"));
		form.set("name", "TestProject");

		client.post(9090, "localhost", "/prj/").sendForm(form).onSuccess(res -> {
			context.verify(() -> {
				flexoProject = res.bodyAsJsonObject();

				Assertions.assertEquals(flexoProject.getString("name"), "TestProject");
				Assertions.assertEquals(res.statusCode(), 200);

				context.completeNow();
			});
		}).onFailure(context::failNow);
	}

	@Test
	@Order(3)
	@Timeout(1000)
	public void initVm(Vertx vertx, VertxTestContext context) {
		WebClient client = WebClient.create(vertx);
		MultiMap form = MultiMap.caseInsensitiveMultiMap();

		form.set("project_id", flexoProject.getString("id"));
		form.set("name", "VirtualModel");
		form.set("is_abstract", "false");
		form.set("visibility", "public");

		client.post(9090, "localhost", "/vm/").sendForm(form).onSuccess(res -> {
			context.verify(() -> {
				vm = res.bodyAsJsonObject();

				Assertions.assertEquals(vm.getString("name"), "VirtualModel");
				Assertions.assertEquals(flexoProject.getString("project_id"), vm.getString("project_id"));
				Assertions.assertEquals(res.statusCode(), 200);

				context.completeNow();
			});
		}).onFailure(context::failNow);
	}

	@Test
	@Order(4)
	@Timeout(1000)
	public void initBhv(Vertx vertx, VertxTestContext context) {
		WebClient client = WebClient.create(vertx);
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		String vmId = vm.getString("id");
		String name = "behaviour1";

		form.set("name", name);
		form.set("is_abstract", "false");
		form.set("visibility", "public");
		form.set("type", "action");

		client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/").sendForm(form).onSuccess(res -> {
			context.verify(() -> {
				behaviour = res.bodyAsJsonObject();

				Assertions.assertEquals(behaviour.getString("visibility"), "Public");
				Assertions.assertEquals(behaviour.getString("is_abstract"), "false");
				Assertions.assertEquals(behaviour.getString("name"), name);
				Assertions.assertEquals(behaviour.getString("virtual_model_id"), vmId);
				Assertions.assertEquals(res.statusCode(), 200);

				context.completeNow();
			});
		}).onFailure(context::failNow);
	}

	@Test
	@Order(5)
	@Timeout(1000)
	public void initProp(Vertx vertx, VertxTestContext context) {
		WebClient client = WebClient.create(vertx);
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		String vmId = vm.getString("id");

		form.set("name", "prop");
		form.set("type", "String");
		form.set("cardinality", "OneMany");

		client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/prp/add-primitive").sendForm(form).onSuccess(res -> {
			context.verify(() -> {
				prop = res.bodyAsJsonObject();

				Assertions.assertEquals(prop.getString("type"), "String");
				Assertions.assertEquals(prop.getString("cardinality"), "OneMany");
				Assertions.assertEquals(prop.getString("name"), "prop");
				Assertions.assertEquals(prop.getString("virtual_model_id"), vmId);
				Assertions.assertEquals(res.statusCode(), 200);

				context.completeNow();
			});
		}).onFailure(context::failNow);
	}

	@Test
	@Order(6)
	@Timeout(1000)
	public void initParam(Vertx vertx, VertxTestContext context) {
		WebClient client = WebClient.create(vertx);
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		String vmId = vm.getString("id");

		form.set("name", "param");
		form.set("is_required", "false");
		form.set("type", "String");

		client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/" + behaviour.getString("signature") + "/param/add-primitive")
				.sendForm(form).onSuccess(res -> {
					context.verify(() -> {
						param = res.bodyAsJsonObject();
						newSignature = param.getString("behaviour_signature");

						Assertions.assertEquals(param.getString("type"), "java.lang.String");
						Assertions.assertEquals(param.getString("is_required"), "false");
						Assertions.assertEquals(param.getString("name"), "param");
						Assertions.assertEquals(res.statusCode(), 200);

						context.completeNow();
					});
				}).onFailure(context::failNow);
	}

	@RepeatedTest(5)
	@Order(7)
	@Timeout(1000)
	public void createLogAction(Vertx vertx, VertxTestContext context) {
		WebClient client = WebClient.create(vertx);
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		String vmId = vm.getString("id");

		form.set("value", "\"Test\"");
		form.set("level", "info");

		client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/" + newSignature + "/act/add-log").sendForm(form)
				.onSuccess(res -> {
					context.verify(() -> {
						action = res.bodyAsJsonObject();

						Assertions.assertEquals(action.getString("resource_type"), "BehaviourAction");
						Assertions.assertEquals(res.statusCode(), 200);

						context.completeNow();
					});
				}).onFailure(context::failNow);
	}

	@RepeatedTest(5)
	@Order(8)
	@Timeout(1000)
	public void createAssignAction(Vertx vertx, VertxTestContext context) {
		WebClient client = WebClient.create(vertx);
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		String vmId = vm.getString("id");

		form.set("left", "prop");
		form.set("right", "parameters.param");

		client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/" + newSignature + "/act/add-assign").sendForm(form)
				.onSuccess(res -> {
					context.verify(() -> {
						action = res.bodyAsJsonObject();

						Assertions.assertEquals(action.getString("resource_type"), "BehaviourAction");
						Assertions.assertEquals(res.statusCode(), 200);

						context.completeNow();
					});
				}).onFailure(context::failNow);
	}

	/*@RepeatedTest(5)
	@Order(9)
	@Timeout(1000)
	public void createAddToListAction(Vertx vertx, VertxTestContext context) {
	    WebClient client    = WebClient.create(vertx);
	    MultiMap form       = MultiMap.caseInsensitiveMultiMap();
	    String vmId         = vm.getString("id");
	
	    form.set("left", "prop");
	    form.set("right", "parameters.param");
	
	    client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/" + newSignature + "/act/add-to-list")
	            .sendForm(form)
	            .onSuccess(res -> {
	                context.verify(() -> {
	                    action = res.bodyAsJsonObject();
	
	                    Assertions.assertEquals(action.getString("resource_type"), "BehaviourAction");
	                    Assertions.assertEquals(res.statusCode(), 200);
	
	                    context.completeNow();
	                });
	            })
	            .onFailure(context::failNow);
	}
	
	@RepeatedTest(5)
	@Order(9)
	@Timeout(1000)
	public void createRemoveFromListAction(Vertx vertx, VertxTestContext context) {
	    WebClient client    = WebClient.create(vertx);
	    MultiMap form       = MultiMap.caseInsensitiveMultiMap();
	    String vmId         = vm.getString("id");
	
	    form.set("left", "prop");
	    form.set("right", "parameters.param");
	
	    client.post(9090, "localhost", "/vm/" + vmId + "/cp/" + vmId + "/bhv/" + newSignature + "/act/remove-from-list")
	            .sendForm(form)
	            .onSuccess(res -> {
	                context.verify(() -> {
	                    action = res.bodyAsJsonObject();
	
	                    Assertions.assertEquals(action.getString("resource_type"), "BehaviourAction");
	                    Assertions.assertEquals(res.statusCode(), 200);
	
	                    context.completeNow();
	                });
	            })
	            .onFailure(context::failNow);
	}*/

}
