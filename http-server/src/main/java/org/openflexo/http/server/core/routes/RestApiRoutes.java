package org.openflexo.http.server.core.routes;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.http.server.HttpService;
import org.openflexo.http.server.RouteService;
import org.openflexo.http.server.core.TechnologyAdapterRouteComplement;
import org.openflexo.http.server.core.controllers.*;
import org.openflexo.http.server.util.IdUtils;
import org.openflexo.http.server.util.ResourceRestService;

import java.util.Collections;
import java.util.Map;

/**
 * A web router, class used to declare web routes.
 */
public class RestApiRoutes implements RouteService<FlexoServiceManager> {

    private ResourceCentersController rcsController;
    private ResourcesController rsController;
    private TechnologyAdaptersController taController;
    private ProjectsController prjController;
    private VirtualModelsController vmController;
    private VirtualModelInstancesController vmiController;
    private ConceptsController cpController;
    private ConceptInstancesController cpiController;
    private PropertiesController prpController;
    private BehavioursController bhvController;
    private ConsoleController cslController;
    private EnumsController enmController;
    private TerminalController cliController;

    /**
     * It creates all the necessary controllers and registers them with the service.
     *
     * @param service the HttpService instance
     * @param serviceManager the service manager that will be used to access the Flexo services
     */
    @Override
    public void initialize(HttpService service, FlexoServiceManager serviceManager) throws Exception {
        rcsController   = new ResourceCentersController(serviceManager.getResourceCenterService(), service.getTechnologyAdapterRestService());
        rsController    = new ResourcesController(serviceManager.getResourceManager(), service.getTechnologyAdapterRestService());
        taController    = new TechnologyAdaptersController();
        prjController   = new ProjectsController(serviceManager.getVirtualModelLibrary(), serviceManager.getProjectLoaderService(), service.getTechnologyAdapterRestService());
        vmController    = new VirtualModelsController(serviceManager.getVirtualModelLibrary());
        vmiController   = new VirtualModelInstancesController(serviceManager.getVirtualModelLibrary());
        cpController    = new ConceptsController(serviceManager.getVirtualModelLibrary());
        cpiController   = new ConceptInstancesController(serviceManager.getVirtualModelLibrary());
        prpController   = new PropertiesController(serviceManager.getVirtualModelLibrary());
        bhvController   = new BehavioursController(serviceManager.getVirtualModelLibrary());
        cslController   = new ConsoleController(serviceManager.getVirtualModelLibrary());
        enmController   = new EnumsController(serviceManager.getVirtualModelLibrary());
        cliController   = new TerminalController(serviceManager.getVirtualModelLibrary());
    }

    /**
     * It adds routes to the router
     *
     * @param vertx the Vert.x instance
     * @param router the router to which the routes will be added
     */
    @Override
    public void addRoutes(Vertx vertx, Router router) {
        router.route().handler(CorsHandler.create(".*."));
        router.route().handler(BodyHandler.create());

        // Resource centers
        router.get("/resource-centers/").produces(JSON).handler(rcsController::list);
        router.get("/rc/").produces(JSON).handler(rcsController::list);
        router.post("/resource-centers/").produces(JSON).handler(rcsController::add);
        router.post("/rc/").produces(JSON).handler(rcsController::add);
        router.post("/resource-centers/upload").produces(JSON).handler(rcsController::upload);
        router.post("/rc/upload").produces(JSON).handler(rcsController::upload);
        router.get("/resource-centers/:rcid").produces(JSON).handler(rcsController::get);
        router.get("/rc/:rcid").produces(JSON).handler(rcsController::get);
        router.get("/resource-centers/:rcid/resource").produces(JSON).handler(rcsController::resources);
        router.get("/rc/:rcid/rsc").produces(JSON).handler(rcsController::resources);
        router.get("/resource-centers/:rcid/resource/*").produces(JSON).handler(rcsController::resourceFolder);
        router.get("/rc/:rcid/rsc/*").produces(JSON).handler(rcsController::resourceFolder);

        // Resources
        router.get("/resource").produces(JSON).handler(rsController::list);
        router.get("/rsc").produces(JSON).handler(rsController::list);
        router.get("/resource/:rid").produces(JSON).handler(rsController::get);
        router.get("/rsc/:rid").produces(JSON).handler(rsController::get);
        router.post("/resource/:rid").produces(JSON).handler(rsController::process);
        router.post("/rsc/:rid").produces(JSON).handler(rsController::process);
        router.get("/resource/:rid/contents").handler(rsController::contents);
        router.get("/rsc/:rid/contents").handler(rsController::contents);

        // Technology Adapters
        router.get("/technology-adapters").produces(JSON).handler(taController::list);
        router.get("/ta").produces(JSON).handler(taController::list);
        router.get("/technology-adapters/:taid").produces(JSON).handler(taController::get);
        router.get("/ta/:taid").produces(JSON).handler(taController::get);

        for (Map.Entry<TechnologyAdapter<?>, TechnologyAdapterRouteComplement> entry : taController.getComplementMap().entrySet()) {
            Router subRouter    = Router.router(vertx);
            String route        = "/ta/" + IdUtils.getTechnologyAdapterId(entry.getKey());
            String shortRoute   = "/technology-adapters/" + IdUtils.getTechnologyAdapterId(entry.getKey());

            router.mountSubRouter(route, subRouter);
            router.mountSubRouter(shortRoute, subRouter);
            entry.getValue().addRoutes(vertx, subRouter);

            for (ResourceRestService service : taController.getRestServices().getOrDefault(entry.getKey(), Collections.emptyList())) {
                service.addRoutes(subRouter);
                taController.getResourcePrefixes().put(service.getResourceClass(), route + service.getPrefix());
            }
        }

        // Projects
        router.get("/projects/").produces(JSON).handler(prjController::list);
        router.get("/prj/").produces(JSON).handler(prjController::list);
        router.get("/projects/:id").produces(JSON).handler(prjController::get);
        router.get("/prj/:id").produces(JSON).handler(prjController::get);
        router.post("/projects/").produces(JSON).handler(prjController::add);
        router.post("/prj/").produces(JSON).handler(prjController::add);
        router.post("/projects/upload").produces(JSON).handler(prjController::upload);
        router.post("/prj/upload").produces(JSON).handler(prjController::upload);
        router.delete("/projects/:id").produces(JSON).handler(prjController::delete);
        router.delete("/prj/:id").produces(JSON).handler(prjController::delete);
        router.get("/projects/:id/resources/").produces(JSON).handler(prjController::resources);
        router.get("/prj/:id/rsc/").produces(JSON).handler(prjController::resources);
        router.post("/projects/:id/resources/load").produces(JSON).handler(prjController::loadResources);
        router.post("/prj/:id/rsc/load").produces(JSON).handler(prjController::loadResources);

        // Folders
        router.get("/projects/:id/folders/").produces(JSON).handler(prjController::folders);
        router.get("/prj/:id/fdr/").produces(JSON).handler(prjController::folders);
        router.post("/projects/:id/folders/").produces(JSON).handler(prjController::addFolder);
        router.post("/prj/:id/fdr/").produces(JSON).handler(prjController::addFolder);
        router.post("/projects/:id/folders/load").produces(JSON).handler(prjController::loadFolders);
        router.post("/prj/:id/fdr/load").produces(JSON).handler(prjController::loadFolders);

        // Virtual Models
        router.get("/virtual-models/").produces(JSON).handler(vmController::list);
        router.get("/vm/").produces(JSON).handler(vmController::list);
        router.get("/virtual-models/:id").produces(JSON).handler(vmController::get);
        router.get("/vm/:id").produces(JSON).handler(vmController::get);
        router.post("/virtual-models/").produces(JSON).handler(vmController::add);
        router.post("/vm/").produces(JSON).handler(vmController::add);
        router.delete("/virtual-models/:id").produces(JSON).handler(vmController::delete);
        router.delete("/vm/:id").produces(JSON).handler(vmController::delete);
        router.get("/virtual-models/:id/fml").produces(JSON).handler(vmController::fml);
        router.get("/vm/:id/fml").produces(JSON).handler(vmController::fml);
        router.post("/virtual-models/:id/fml").produces(JSON).handler(vmController::saveFml);
        router.post("/vm/:id/fml").produces(JSON).handler(vmController::saveFml);

        // Enums
        router.get("/virtual-models/:vmid/enums/").produces(JSON).handler(enmController::list);
        router.get("/vm/:vmid/enums/").produces(JSON).handler(enmController::list);
        router.get("/virtual-models/:vmid/enums/:id").produces(JSON).handler(enmController::get);
        router.get("/vm/:vmid/enums/:id").produces(JSON).handler(enmController::get);
        router.post("/virtual-models/:vmid/enums/").produces(JSON).handler(enmController::add);
        router.post("/vm/:vmid/enums/").produces(JSON).handler(enmController::add);
        router.delete("/virtual-models/:vmid/enums/:id").produces(JSON).handler(enmController::delete);
        router.delete("/vm/:vmid/enums/:id").produces(JSON).handler(enmController::delete);
        router.get("/virtual-models/:vmid/enums/:id/values/").produces(JSON).handler(enmController::values);
        router.get("/vm/:vmid/enums/:id/values/").produces(JSON).handler(enmController::values);
        router.post("/virtual-models/:vmid/enums/:id/values/").produces(JSON).handler(enmController::addValue);
        router.post("/vm/:vmid/enums/:id/values/").produces(JSON).handler(enmController::addValue);

        // Concepts
        router.get("/virtual-models/:vmid/concepts/").produces(JSON).handler(cpController::list);
        router.get("/vm/:vmid/cp/").produces(JSON).handler(cpController::list);
        router.get("/virtual-models/:vmid/concepts/:id").produces(JSON).handler(cpController::get);
        router.get("/vm/:vmid/cp/:id").produces(JSON).handler(cpController::get);
        router.post("/virtual-models/:vmid/concepts/").produces(JSON).handler(cpController::add);
        router.post("/vm/:vmid/cp/").produces(JSON).handler(cpController::add);
        router.patch("/virtual-models/:vmid/concepts/:id/").produces(JSON).handler(cpController::edit);
        router.patch("/vm/:vmid/cp/:id/").produces(JSON).handler(cpController::edit);
        router.delete("/virtual-models/:vmid/concepts/:id").produces(JSON).handler(cpController::delete);
        router.delete("/vm/:vmid/cp/:id").produces(JSON).handler(cpController::delete);

        // properties
        router.get("/virtual-models/:vmid/concepts/:id/properties/").produces(JSON).handler(prpController::list);
        router.get("/vm/:vmid/cp/:id/prp/").produces(JSON).handler(prpController::list);
        router.get("/virtual-models/:vmid/concepts/:id/properties/:name").produces(JSON).handler(prpController::get);
        router.get("/vm/:vmid/cp/:id/prp/:name").produces(JSON).handler(prpController::get);
        router.post("/virtual-models/:vmid/concepts/:id/properties/add-primitive").produces(JSON).handler(prpController::addPrimitive);
        router.post("/vm/:vmid/cp/:id/prp/add-primitive").produces(JSON).handler(prpController::addPrimitive);
        router.post("/virtual-models/:vmid/concepts/:id/properties/add-flexo-concept-instance-role").produces(JSON).handler(prpController::addFlexoConceptInstanceRole);
        router.post("/vm/:vmid/cp/:id/prp/add-fcir").produces(JSON).handler(prpController::addFlexoConceptInstanceRole);
        router.post("/virtual-models/:vmid/concepts/:id/properties/add-model-slot").produces(JSON).handler(prpController::addModelSlot);
        router.post("/vm/:vmid/cp/:id/prp/add-ms").produces(JSON).handler(prpController::addModelSlot);
        router.delete("/virtual-models/:vmid/concepts/:id/properties/:name").produces(JSON).handler(prpController::delete);
        router.delete("/vm/:vmid/cp/:id/prp/:name").produces(JSON).handler(prpController::delete);

        // Behaviours
        router.get("/virtual-models/:vmid/concepts/:id/behaviours/").produces(JSON).handler(bhvController::list);
        router.get("/vm/:vmid/cp/:id/bhv/").produces(JSON).handler(bhvController::list);
        router.get("/virtual-models/:vmid/concepts/:id/behaviours/:signature").produces(JSON).handler(bhvController::get);
        router.get("/vm/:vmid/cp/:id/bhv/:signature").produces(JSON).handler(bhvController::get);
        router.post("/virtual-models/:vmid/concepts/:id/behaviours/").produces(JSON).handler(bhvController::add);
        router.post("/vm/:vmid/cp/:id/bhv/").produces(JSON).handler(bhvController::add);
        router.delete("/virtual-models/:vmid/concepts/:id/behaviours/:signature").produces(JSON).handler(bhvController::delete);
        router.delete("/vm/:vmid/cp/:id/bhv/:signature").produces(JSON).handler(bhvController::delete);

        // Parameters
        router.get("/virtual-models/:vmid/concepts/:id/behaviours/:signature/parameters/").produces(JSON).handler(bhvController::parameters);
        router.get("/vm/:vmid/cp/:id/bhv/:signature/param/").produces(JSON).handler(bhvController::parameters);
        router.get("/virtual-models/:vmid/concepts/:id/behaviours/:signature/parameters/:name").produces(JSON).handler(bhvController::parameter);
        router.get("/vm/:vmid/cp/:id/bhv/:signature/param/:name").produces(JSON).handler(bhvController::parameter);
        router.post("/virtual-models/:vmid/concepts/:id/behaviours/:signature/parameters/add-primitive").produces(JSON).handler(bhvController::addPrimitiveParameter);
        router.post("/vm/:vmid/cp/:id/bhv/:signature/param/add-primitive").produces(JSON).handler(bhvController::addPrimitiveParameter);
        router.post("/virtual-models/:vmid/concepts/:id/behaviours/:signature/parameters/add-fml-instance").produces(JSON).handler(bhvController::addFmlInstance);
        router.post("/vm/:vmid/cp/:id/bhv/:signature/param/add-fmli").produces(JSON).handler(bhvController::addFmlInstance);
        router.post("/virtual-models/:vmid/concepts/:id/behaviours/:signature/parameters/add-fml-enum").produces(JSON).handler(bhvController::addFmlEnum);
        router.post("/vm/:vmid/cp/:id/bhv/:signature/param/add-fmle").produces(JSON).handler(bhvController::addFmlEnum);
        router.delete("/virtual-models/:vmid/concepts/:id/behaviours/:signature/parameters/:name").produces(JSON).handler(bhvController::deleteParameter);
        router.delete("/vm/:vmid/cp/:id/bhv/:signature/param/:name").produces(JSON).handler(bhvController::deleteParameter);

        // Actions
        router.post("/virtual-models/:vmid/concepts/:id/behaviours/:signature/actions/add-log").produces(JSON).handler(bhvController::addLogAction);
        router.post("/vm/:vmid/cp/:id/bhv/:signature/act/add-log").produces(JSON).handler(bhvController::addLogAction);
        router.post("/virtual-models/:vmid/concepts/:id/behaviours/:signature/actions/add-assignation").produces(JSON).handler(bhvController::addAssignation);
        router.post("/vm/:vmid/cp/:id/bhv/:signature/act/add-assign").produces(JSON).handler(bhvController::addAssignation);
        router.post("/virtual-models/:vmid/concepts/:id/behaviours/:signature/actions/add-to-list").produces(JSON).handler(bhvController::addToList);
        router.post("/vm/:vmid/cp/:id/bhv/:signature/act/add-to-list").produces(JSON).handler(bhvController::addToList);
        router.post("/virtual-models/:vmid/concepts/:id/behaviours/:signature/actions/remove-from-list").produces(JSON).handler(bhvController::removeFromList);
        router.post("/vm/:vmid/cp/:id/bhv/:signature/act/remove-from-list").produces(JSON).handler(bhvController::removeFromList);

        // Virtual Model Instances
        router.get("/projects/:prjid/virtual-model/:vmid/instances/").produces(JSON).handler(vmiController::list);
        router.get("/prj/:prjid/vm/:vmid/instances/").produces(JSON).handler(vmiController::list);
        router.get("/projects/:prjid/virtual-model/:vmid/instances/:id").produces(JSON).handler(vmiController::get);
        router.get("/prj/:prjid/vm/:vmid/instances/:id").produces(JSON).handler(vmiController::get);
        router.post("/projects/:prjid/virtual-model/:vmid/instances/").produces(JSON).handler(vmiController::add);
        router.post("/prj/:prjid/vm/:vmid/instances/").produces(JSON).handler(vmiController::add);
        router.delete("/projects/:prjid/virtual-model/:vmid/instances/:id").produces(JSON).handler(vmiController::delete);
        router.delete("/prj/:prjid/vm/:vmid/instances/:id").produces(JSON).handler(vmiController::delete);
        router.get("/projects/:prjid/virtual-model/:vmid/instances/:id/behaviours/").produces(JSON).handler(vmiController::behaviours);
        router.get("/prj/:prjid/vm/:vmid/instances/:id/bhv/").produces(JSON).handler(vmiController::behaviours);

        router.post("/projects/:prjid/virtual-model/:vmid/instances/:id/behaviours/:signature/execute").produces(JSON).handler(vmiController::executeBehaviour);
        router.post("/prj/:prjid/vm/:vmid/instances/:id/bhv/:signature/exe").produces(JSON).handler(vmiController::executeBehaviour);


        // Concept Instances
        router.post("/concept-instances/").produces(JSON).handler(cpiController::add);
        router.post("/cpi/").produces(JSON).handler(cpiController::add);
        router.get("/concept-instances/").produces(JSON).handler(cpiController::list);
        router.get("/cpi/").produces(JSON).handler(cpiController::list);
        router.get("/concept-instances/:id").produces(JSON).handler(cpiController::get);
        router.get("/cpi/:id").produces(JSON).handler(cpiController::get);
        router.delete("/concept-instances/:id/").produces(JSON).handler(cpiController::delete);
        router.delete("/cpi/:id/").produces(JSON).handler(cpiController::delete);

        router.get("/console").produces(JSON).handler(cslController::show);

        router.post("/kill-server").produces(JSON).handler(cslController::shutdown);

        router.post("/terminal/get").produces(JSON).handler(cliController::get);
        router.post("/terminal/init").produces(JSON).handler(cliController::init);
        router.post("/terminal/execute").produces(JSON).handler(cliController::execute);
        router.post("/terminal/history").produces(JSON).handler(cliController::history);
        router.post("/terminal/completion").produces(JSON).handler(cliController::completion);
        router.post("/terminal/kill").produces(JSON).handler(cliController::kill);
    }
}
