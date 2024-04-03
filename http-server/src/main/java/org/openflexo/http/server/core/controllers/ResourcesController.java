package org.openflexo.http.server.core.controllers;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.http.server.core.TechnologyAdapterRouteService;
import org.openflexo.http.server.json.JsonUtils;
import org.openflexo.http.server.util.IdUtils;
import org.openflexo.rm.Resource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *  Resources rest apis controller.
 * @author Ihab Benamer
 */
public class ResourcesController extends GenericController{

    private final ResourceManager resourceManager;
    private final TechnologyAdapterRouteService technologyAdapterRestService;

    /**
     * Instantiates a new Resources controller.
     *
     * @param resourceManager              the resource manager
     * @param technologyAdapterRestService the technology adapter rest service
     */
    public ResourcesController(ResourceManager resourceManager, TechnologyAdapterRouteService technologyAdapterRestService) {
        this.resourceManager                = resourceManager;
        this.technologyAdapterRestService   = technologyAdapterRestService;
    }

    /**
     * It creates a JSON array, iterates over all registered resources, and adds a JSON object to the array for each
     * resource
     *
     * @param context the context of the request, which contains the request and response objects.
     */
    public void list(RoutingContext context) {
        JsonArray result = new JsonArray();
        for (FlexoResource<?> resource : resourceManager.getRegisteredResources()) {
            result.add(JsonUtils.getResourceDescription(resource, technologyAdapterRestService));
        }
        context.response().end(result.encodePrettily());
    }

    /**
     * It gets the resource id from the request, decodes it, gets the resource from the resource manager, and if it exists,
     * returns a JSON representation of the resource
     *
     * @param context the routing context
     */
    public void get(RoutingContext context) {
        String id = context.request().getParam("rid").trim();
        String uri = IdUtils.decodeId(id);

        FlexoResource<?> resource = resourceManager.getResource(uri);
        if (resource != null) {
            context.response().end(JsonUtils.getResourceDescription(resource, technologyAdapterRestService).encodePrettily());
        }
        else {
            notFound(context);
        }
    }

    /**
     * It gets the resource id from the request, decodes it, gets the resource from the resource manager, saves it, and
     * returns the resource description
     *
     * @param context the routing context
     */
    public void process(RoutingContext context) {
        String id = context.request().getParam("rid").trim();
        String uri = IdUtils.decodeId(id);

        FlexoResource<?> resource = resourceManager.getResource(uri);
        if (resource != null) {
            try {
                resource.save();
                context.response().end(JsonUtils.getResourceDescription(resource, technologyAdapterRestService).encodePrettily());
            } catch (SaveResourceException e) {
                error(context, e);
            }
        }
        else {
            notFound(context);
        }
    }

    /**
     * It reads the resource from the resource manager, and sends it to the client
     *
     * @param context the routing context
     */
    public void contents(RoutingContext context) {
        String id = context.request().getParam("rid").trim();
        String uri = IdUtils.decodeId(id);

        FlexoResource<?> resource = resourceManager.getResource(uri);
        if (resource != null) {
            context.response().putHeader("Content-Disposition", "filename=\"" + resource.getName() + "\"");
            Resource artefactAsResource = resource.getIODelegate().getSerializationArtefactAsResource();
            try (InputStream inputStream = new BufferedInputStream(artefactAsResource.openInputStream())) {
                Buffer buffer = Buffer.buffer();
                int read = inputStream.read();
                while (read >= 0) {
                    buffer.appendByte((byte) read);
                    read = inputStream.read();
                }
                HttpServerResponse response = context.response();
                response.putHeader("Content-Length", Integer.toString(buffer.length()));
                response.end(buffer);
            } catch (IOException e) {
                context.fail(e);
            }
        }
        else {
            notFound(context);
        }
    }
}
