package org.openflexo.http.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import jline.internal.Log;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.http.server.core.routes.RestApiRoutes;
import org.openflexo.logging.FlexoLoggingManager;

/**
 * Main class for OpenFlexo server
 */
public class OpenFlexoServer extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		Options options 							= new Options();
		options.serverOptions.host 					= "localhost";
		options.serverOptions.port 					= 9090;
		FlexoServiceManager manager 				= createServiceManager(options);
		Router router 								= Router.router(vertx);
		ServiceLoader<RouteService> restServices 	= ServiceLoader.load(RouteService.class);
		List<RouteService> initializedServices 		= new ArrayList<>();

		for (RouteService<FlexoServiceManager> routeService : restServices) {
			if(routeService instanceof RestApiRoutes) {
				routeService.initialize(new HttpService(options.serverOptions), manager);
				initializedServices.add(routeService);
			}
		}

		initializedServices.get(0).addRoutes(vertx, router);

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(router).listen(9090, "localhost");
	}

	public static class Options {

		public HttpService.Options serverOptions = new HttpService.Options();

		public boolean verbose = false;

		public boolean enableDirectoryWatching = true;

		public boolean devMode = false;

		public final List<String> centerPaths = new ArrayList<>();

		public final List<String> projectPaths = new ArrayList<>();
				
		public static FlexoEditor globalEditor;

		public String preloadFilePath = null;

	}

	public static FlexoServiceManager createServiceManager(Options options) {
		FlexoServiceManager manager = new DefaultFlexoServiceManager(null, options.enableDirectoryWatching, options.devMode);
		TechnologyAdapterService technologyAdapterService = manager.getTechnologyAdapterService();
		technologyAdapterService.activateTechnologyAdapter(technologyAdapterService.getTechnologyAdapter(FMLTechnologyAdapter.class), true);
		technologyAdapterService.activateTechnologyAdapter(technologyAdapterService.getTechnologyAdapter(FMLRTTechnologyAdapter.class),
				true);
		return manager;
	}

	private static void usage() {
		StringBuilder usage = new StringBuilder();
		usage.append("Usage: server [options]\n");
		usage.append("\n");
		usage.append("- -h|--help: show this help.\n");
		usage.append("- -v|--verbose: verbose mode.\n");
		usage.append("- --port port: server port.\n");
		usage.append("- --host host: server host.\n");
		usage.append("- --center path: resource center to register (may have several).\n");
		usage.append("- --project path: path for a project to open (may have several).\n");
		usage.append("- --preload file: Path with resources to load at startup (one URI per line).\n");
		usage.append("\n");
		usage.append("\n");

		System.out.println(usage);
		System.exit(0);
	}

	private static Options parseOptions(String[] args) {
		Options options = new Options();

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			switch (arg) {
				case "--help":
					usage();
					break;
				case "--verbose":
					options.verbose = true;
					break;
				case "--port":
					if (i + 1 < args.length) {
						try {
							options.serverOptions.port = Integer.parseInt(args[++i]);
						} catch (NumberFormatException e) {
							System.err.println("Port must be an integer.");
							System.exit(1);
						}
					}
					else {
						System.err.println("Option " + arg + " needs an argument.");
						System.exit(1);
					}
					break;
				case "--host":
					if (i + 1 < args.length) {
						options.serverOptions.host = args[++i];
					}
					else {
						System.err.println("Option " + arg + " needs an argument.");
						System.exit(1);
					}
					break;
				case "--center":
					if (i + 1 < args.length) {
						options.centerPaths.add(args[++i]);
					}
					else {
						System.err.println("Option " + arg + " needs an argument.");
						System.exit(1);
					}
					break;
				case "--project":
					if (i + 1 < args.length) {
						options.projectPaths.add(args[++i]);
					}
					else {
						System.err.println("Option " + arg + " needs an argument.");
						System.exit(1);
					}
					break;
				case "--preload":
					if (i + 1 < args.length) {
						options.preloadFilePath = args[++i];
					}
					else {
						System.err.println("Option " + arg + " needs an argument.");
						System.exit(1);
					}
					break;
				default: {
					if (arg.length() >= 2 && arg.charAt(0) == '-' && arg.charAt(1) != '-') {
						for (int j = 1; j < arg.length(); j++) {
							switch (arg.charAt(j)) {
								case 'h':
									usage();
									break;
								case 'v':
									options.verbose = true;
									break;
								default:
									System.err.println("Unknown short option '" + arg.charAt(j) + "'");
									System.exit(1);
							}
						}
					}
					else {
						System.err.println("Unknown long option '" + arg + "'");
						System.exit(1);
					}
				}

			}
		}

		return options;
	}

	public static void main(String[] args) throws IOException {
		Options options = parseOptions(args);

		FlexoLoggingManager.initialize(-1, true, null, options.verbose ? Level.INFO : Level.WARNING, null);

		FlexoServiceManager manager = createServiceManager(options);
		manager.registerService(new HttpService(options.serverOptions));

		for (String path : options.centerPaths) {
			FlexoResourceCenterService centerService = manager.getResourceCenterService();
			DirectoryResourceCenter center = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(new File(path), centerService);
			centerService.addToResourceCenters(center);
		}

		for (String path : options.projectPaths) {
			try {
				
				Options.globalEditor = manager.getProjectLoaderService().loadProject(new File(path));
			} catch (ProjectLoadingCancelledException | ProjectInitializerException e) {
				System.err.println("[ERROR] Can't load project '" + path + "'.");
			}
		}

		if(options.preloadFilePath != null) {
			startupPreload(options.preloadFilePath);
		}
	}

	private static void startupPreload(String preloadFilePath) {
		// TEST FLX - preload specific resources

		List<String> resourcesUrisToPreload = new ArrayList<>();


		// Load preload URIs from file
		try {
			Path path = Paths.get(preloadFilePath);
			Stream<String> lines = Files.lines(path);
			resourcesUrisToPreload = lines.collect(Collectors.toList());
		} catch (Exception e) {
			System.err.println("[ERROR] Cannot read the preload file : " + preloadFilePath);
			e.printStackTrace();
			System.exit(127);
		}

		/*

		String resourcesUrisToPreload [] = {"http://www.openflexo.org/projects/2021/12/sse4space_1638887944201.prj/Lifecycle.fml",
										"http:://telindus.lu/SSE4Space/SSE4Space/RBAC.fml",
										"http:://telindus.lu/SSE4Space/SSE4Space/MinioVersionModel.fml",
										"http:://telindus.lu/SSE4Space/SSE4Space"};
		*/

		ResourceManager resourceManager = Options.globalEditor.getServiceManager().getResourceManager();
		List<FlexoResource<?>> priorityList = new ArrayList<>();

		// Check if resource exist
		for(String resourceUri : resourcesUrisToPreload) {
			FlexoResource currentRes = resourceManager.getResource(resourceUri);

			if(currentRes != null) {
				priorityList.add(currentRes);
			} else {
				System.err.println("[ERROR] A resource to preload does not exists in the context : " + resourceUri);
				System.exit(127);
			}
		}

		// Preload priority resources
		preloadResourcesInOrder(priorityList);

		// Preload all others resources
		preloadResourcesInOrder(resourceManager.getRegisteredResources());
	}

	private static void preloadResourcesInOrder(List<FlexoResource<?>> listResources) {
		for(FlexoResource<?> r : listResources) {
			try {
				System.out.println("Preloading resource : " + r.getName());
				r.loadResourceData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
