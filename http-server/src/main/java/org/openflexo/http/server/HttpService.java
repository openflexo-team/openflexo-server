package org.openflexo.http.server;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Created by charlie on 17/01/2017.
 */
public class HttpService extends FlexoServiceImpl implements FlexoService {

	private static final String JSON = "application/json";

	private static Logger logger = Logger.getLogger(HttpService.class.getPackage().getName());

	public static class Options {
		public int port = 8080;

		public String host = "localhost";

		public String webDirectory = "./webroot";
	}

	private final int port;
	private final String host;

	private final Path webPath;

	private final Vertx vertx = Vertx.vertx();
	private final HttpServerOptions serverOptions;

	private HttpServer server = null;
	private Router router = null;

	public HttpService(Options options) {
		this.port = options.port;
		this.host = options.host;
		serverOptions = new HttpServerOptions();

		this.webPath = Paths.get(options.webDirectory);
	}

	@Override
	public void initialize() {
		router = Router.router(vertx);
		router.get("/center").produces(JSON).handler(this::serveCenterList);
		router.get("/center/:cid").produces(JSON).handler(this::serveCenter);

		// TODO add support for path
		router.get("/center/:cid/resource").produces(JSON).handler(this::serveCenterResourceList);

		router.get("/resource").produces(JSON).handler(this::serveResourceList);
		router.get("/resource/:rid").produces(JSON).handler(this::serveResource);

		router.get("/*").handler(StaticHandler.create());

		server = vertx.createHttpServer(serverOptions);
		server.requestHandler(router::accept);

		logger.info("Starting HTTP Server on " + host + ":" + port);
		server.listen(port, host);
	}

	private Path resolveWebPath(String relativePath) {
		// trim "/" a the beginning
		Path result = webPath.resolve(relativePath.substring(1));
		// ensure that result is child of web path and that the file exists
		if (!result.startsWith(webPath) || !Files.exists(result)) return null;
		return result;
	}

	private void serveCenterList(RoutingContext context) {
		JsonArray result = new JsonArray();
		for (FlexoResourceCenter<?> center : getServiceManager().getResourceCenterService().getResourceCenters()) {
			result.add(JsonUtils.getCenterDescription(center));
		}
		context.response().end(result.encodePrettily());
	}

	private void serveCenter(RoutingContext context) {
		String centerId = context.request().getParam(("cid"));
		String uri = IdUtils.decodeId(centerId);

		FlexoResourceCenter<?> resourceCenter = getServiceManager().getResourceCenterService().getFlexoResourceCenter(uri);
		if (resourceCenter != null) {
			context.response().end(JsonUtils.getCenterDescription(resourceCenter).encodePrettily());
		} else {
			notFound(context);
		}
	}

	private void serveCenterResourceList(RoutingContext context) {
		String centerId = context.request().getParam(("cid"));
		String centerUri = IdUtils.decodeId(centerId);

		FlexoResourceCenter<?> resourceCenter = getServiceManager().getResourceCenterService().getFlexoResourceCenter(centerUri);
		if (resourceCenter != null) {
			JsonArray result = new JsonArray();
			for (FlexoResource<?> resource : resourceCenter.getAllResources(null)) {
				result.add(JsonUtils.getResourceDescription(resource));
			}
			context.response().end(result.encodePrettily());
		} else {
			notFound(context);
		}
	}

	private void serveResourceList(RoutingContext context) {
		JsonArray result = new JsonArray();
		for (FlexoResource<?> resource : getServiceManager().getResourceManager().getRegisteredResources()) {
			result.add(JsonUtils.getResourceDescription(resource));
		}
		context.response().end(result.encodePrettily());
	}

	private void serveResource(RoutingContext context) {
		String id = context.request().getParam(("rid"));
		String uri = IdUtils.decodeId(id);

		FlexoResource<?> resource = getServiceManager().getResourceManager().getResource(uri);
		if (resource != null) {
			context.response().end(JsonUtils.getResourceDescription(resource).encodePrettily());
		} else {
			notFound(context);
		}
	}


	private void notFound(RoutingContext context) {
		context.response().setStatusCode(404).close();
	}

	@Override
	public void stop() {
		server.close();
	}


}
