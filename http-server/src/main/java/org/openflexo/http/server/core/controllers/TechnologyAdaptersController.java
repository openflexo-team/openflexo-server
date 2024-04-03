package org.openflexo.http.server.core.controllers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.http.server.core.TechnologyAdapterRouteComplement;
import org.openflexo.http.server.core.TechnologyAdapterRouteService;
import org.openflexo.http.server.json.JsonUtils;
import org.openflexo.http.server.util.ResourceRestService;
import org.openflexo.toolbox.StringUtils;

import java.util.*;
import java.util.logging.Logger;

public class TechnologyAdaptersController extends GenericController{

    private static Logger logger = Logger.getLogger(TechnologyAdapterRouteService.class.getPackage().getName());


    /** Map of technology adapters to corresponding {@link TechnologyAdapterRouteComplement} */
    private final Map<TechnologyAdapter<?>, TechnologyAdapterRouteComplement> complementMap = new LinkedHashMap<>();

    /** Map of prefix to corresponding {@link TechnologyAdapter} */
    private final Map<String, TechnologyAdapter<?>> technologyAdapterMap = new LinkedHashMap<>();

    /** Map of registered {@link org.openflexo.http.server.util.PamelaResourceRestService}s for each technology adapters */
    private final Map<TechnologyAdapter<?>, List<ResourceRestService>> restServices = new HashMap<>();


    /** Map of FlexoResource classes to the prefix for this type of resource */
    private final Map<Class<? extends FlexoResource<?>>, String> resourcePrefixes = new TreeMap<>(Comparator.comparing(Class::getSimpleName));


//    public void registerPamelaResourceRestService(TechnologyAdapter<?> adapter, ResourceRestService<?, ?> service) {
//        restServices.computeIfAbsent(adapter, (a) -> new ArrayList<>()).add(service);
//    }
////
//    public void complementTechnologyAdapter(TechnologyAdapter<?> adapter, JsonObject result) {
//        TechnologyAdapterRouteComplement complement = complementMap.get(adapter);
//        result.put("complemented", complement != null);
//        for (ResourceRestService service : restServices.getOrDefault(adapter, Collections.emptyList())) {
//            String simpleName = service.getResourceClass().getSimpleName();
//            String route = result.getString("url") + service.getPrefix();
//            result.put(StringUtils.firstsLower(simpleName) + "Url", route);
//        }
//    }
////
//    public String getPrefix(FlexoResource<?> resource) {
//        if (resource == null)
//            return null;
//        for (Map.Entry<Class<? extends FlexoResource<?>>, String> entry : resourcePrefixes.entrySet()) {
//            if (entry.getKey().isAssignableFrom(resource.getClass())) {
//                return entry.getValue();
//            }
//        }
//        return null;
//    }
//
    public void list(RoutingContext context) {
        JsonArray result = new JsonArray();
        for (Map.Entry<String, TechnologyAdapter<?>> entry : technologyAdapterMap.entrySet()) {
            TechnologyAdapter<?> technologyAdapter = entry.getValue();
            result.add(JsonUtils.getTechnologyAdapterDescription(entry.getKey(), technologyAdapter));
        }
        context.response().end(result.encodePrettily());
    }

    public void get(RoutingContext context) {
        String id = context.request().getParam(("taid"));
        TechnologyAdapter<?> technologyAdapter = technologyAdapterMap.get(id);
        if (technologyAdapter != null) {
            JsonObject object = JsonUtils.getTechnologyAdapterDescription(id, technologyAdapter);
            context.response().end(object.encodePrettily());
        }
        else {
            notFound(context);
        }
    }

    public Map<TechnologyAdapter<?>, TechnologyAdapterRouteComplement> getComplementMap() {
        return complementMap;
    }

    public Map<TechnologyAdapter<?>, List<ResourceRestService>> getRestServices() {
        return restServices;
    }

    public Map<Class<? extends FlexoResource<?>>, String> getResourcePrefixes() {
        return resourcePrefixes;
    }
}
