package org.openflexo.http.server.core;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.HashMap;
import java.util.Map;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.http.server.RestService;

/**
 * Created by charlie on 11/02/2017.
 */
public class TechnologyAdapterRestService implements RestService {

	private TechnologyAdapterService technologyAdapterService;

	private final Map<String, TechnologyAdapter> technologyAdapterMap = new HashMap<>();

	@Override
	public void initialize(FlexoServiceManager serviceManager) throws Exception {
		technologyAdapterService = serviceManager.getTechnologyAdapterService();
		for (TechnologyAdapter technologyAdapter : technologyAdapterService.getTechnologyAdapters()) {
			String identifier = IdUtils.getTechnologyAdapterId(technologyAdapter);
			technologyAdapterMap.put(identifier, technologyAdapter);
		}
	}

	@Override
	public void addRoutes(Vertx vertx, Router router) {
		router.get("/ta").produces(JSON).handler(this::serveTechnologyAdapterList);
		router.get("/ta/:taid").produces(JSON).handler(this::serveTechnologyAdapter);
	}

	private void serveTechnologyAdapterList(RoutingContext context) {
		JsonArray result = new JsonArray();
		for (TechnologyAdapter technologyAdapter : technologyAdapterService.getTechnologyAdapters()) {
			result.add(JsonUtils.getTechnologyAdapterDescription(technologyAdapter));
		}
		context.response().end(result.encodePrettily());
	}

	private void serveTechnologyAdapter(RoutingContext context) {
		String id = context.request().getParam(("taid"));
		TechnologyAdapter technologyAdapter = technologyAdapterMap.get(id);
		if (technologyAdapter != null) {
			context.response().end(JsonUtils.getTechnologyAdapterDescription(technologyAdapter).encodePrettily());
		}
		else {
			notFound(context);
		}
	}
}
