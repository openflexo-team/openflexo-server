package org.openflexo.http.server.core.controllers;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.*;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.http.server.core.helpers.Helpers;
import org.openflexo.http.server.core.serializers.JsonSerializer;
import org.openflexo.http.server.core.validators.ConceptInstancesValidator;

/**
 *  Concept Instances rest apis controller.
 * @author Ihab Benamer
 */
public class ConceptInstancesController extends GenericController {
    private final VirtualModelLibrary virtualModelLibrary;
    private final DefaultFlexoEditor editor;

    /**
     * Instantiates a new Concept instances controller.
     *
     * @param virtualModelLibrary the virtual model library
     */
    public ConceptInstancesController(VirtualModelLibrary virtualModelLibrary) {
        this.virtualModelLibrary    = virtualModelLibrary;
        editor                      = Helpers.getDefaultFlexoEditor(virtualModelLibrary);
    }

    /**
     * It iterates over every project, virtual model instance and gathers all the contained flexo concept instances,
     * serializes them to JSON and sends them back to the client.
     *
     * @param context the routing context
     */
    public void list(RoutingContext context) {
        JsonArray result = new JsonArray();

        for (FlexoProject<?> project : virtualModelLibrary.getServiceManager().getProjectLoaderService().getRootProjects()) {
            for (FMLRTVirtualModelInstanceResource vmiRes : project.getVirtualModelInstanceRepository().getAllResources()) {
                try {
                    FMLRTVirtualModelInstance vmi = vmiRes.getVirtualModelInstance();
                    for (FlexoConceptInstance fci : vmi.getFlexoConceptInstances()) {
                        result.add(JsonSerializer.conceptInstanceSerializer(fci));
                    }
                } catch (Exception e) {
                    // Ignore instances that cannot be loaded
                }
            }
        }

        context.response().end(result.encodePrettily());
    }

    /**
     * It looks up a flexo concept instance by its id (flexo id) across every project and virtual model instance, then
     * serializes it to JSON.
     *
     * @param context the routing context
     */
    public void get(RoutingContext context) {
        String id = context.request().getParam("id");

        for (FlexoProject<?> project : virtualModelLibrary.getServiceManager().getProjectLoaderService().getRootProjects()) {
            for (FMLRTVirtualModelInstanceResource vmiRes : project.getVirtualModelInstanceRepository().getAllResources()) {
                try {
                    FMLRTVirtualModelInstance vmi = vmiRes.getVirtualModelInstance();
                    for (FlexoConceptInstance fci : vmi.getFlexoConceptInstances()) {
                        if (String.valueOf(fci.getFlexoID()).equals(id)) {
                            context.response().end(JsonSerializer.conceptInstanceSerializer(fci).encodePrettily());
                            return;
                        }
                    }
                } catch (Exception e) {
                    // Ignore instances that cannot be loaded
                }
            }
        }

        notFound(context);
    }

    /**
     * It creates a new instance of a FlexoConcept in a VirtualModelInstance
     *
     * @param context the routing context
     */
    public void add(RoutingContext context) {

        ConceptInstancesValidator validator = new ConceptInstancesValidator(context.request(), virtualModelLibrary);
        JsonArray errors                   = validator.validate();

        if(validator.isValid()){
            CreateFlexoConceptInstance action   = CreateFlexoConceptInstance.actionType.makeNewAction(validator.getContainer(), null, editor);
            action.setFlexoConcept(validator.getConcept());

            if (validator.getConcept().getCreationSchemes().size() > 0) {
                CreationScheme cs = validator.getConcept().getCreationSchemes().get(0);
                action.setCreationScheme(cs);
            }

            action.doAction();

            try {
                validator.getContainer().getResource().save();
            } catch (SaveResourceException e) {
                throw new RuntimeException(e);
            }

            context.response().end(JsonSerializer.conceptInstanceSerializer(action.getNewFlexoConceptInstance()).encodePrettily());
        } else {
            badValidation(context, errors);
        }
    }

    public void edit(RoutingContext context) {}

    public void delete(RoutingContext context) {}

}
