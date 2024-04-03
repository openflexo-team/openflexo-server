package org.openflexo.http.server.core.controllers;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.*;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.DeleteFlexoConceptObjects;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.http.server.core.helpers.Helpers;
import org.openflexo.http.server.core.serializers.JsonSerializer;
import org.openflexo.http.server.core.validators.ConceptsValidator;
import org.openflexo.http.server.util.IdUtils;

import java.io.FileNotFoundException;

/**
 *  Concepts rest apis controller.
 * @author Ihab Benamer
 */
public class ConceptsController extends GenericController {
    private final VirtualModelLibrary virtualModelLibrary;
    private final DefaultFlexoEditor editor;

    /**
     * Instantiates a new Concepts controller.
     *
     * @param virtualModelLibrary the virtual model library
     */
    public ConceptsController(VirtualModelLibrary virtualModelLibrary) {
        this.virtualModelLibrary    = virtualModelLibrary;
        editor                      = Helpers.getDefaultFlexoEditor(virtualModelLibrary);
    }

    /**
     * It gets the virtual model from the library, then serializes all the concepts in it and returns the result
     *
     * @param context the routing context
     */
    public void list(RoutingContext context) {
        String id = context.request().getParam("vmid").trim();
        try {
            VirtualModel model  = virtualModelLibrary.getVirtualModel(IdUtils.decodeId(id));
            JsonArray result    = new JsonArray();

            for (FlexoConcept concept : model.getFlexoConcepts()) {
                result.add(JsonSerializer.conceptSerializer(concept));
            }

            context.response().end(result.encodePrettily());
        } catch (Exception e) {
            notFound(context);
        }
    }

    /**
     * Get the concept with the given id and return it as a JSON object
     *
     * @param context the routing context
     */
    public void get(RoutingContext context) {
        String id               = context.request().getParam("id").trim();
        FlexoConcept concept    = virtualModelLibrary.getFlexoConcept(IdUtils.decodeId(id));

        if (concept != null){
            context.response().end(JsonSerializer.conceptSerializer(concept).encodePrettily());
        } else {
            notFound(context);
        }
    }

    /**
     * It adds a new concept to a virtual model
     *
     * @param context the routing context
     */
    public void add(RoutingContext context) {
        String id = context.request().getParam("vmid");

        try {
            VirtualModel model          = virtualModelLibrary.getVirtualModel(IdUtils.decodeId(id));
            ConceptsValidator validator = new ConceptsValidator(context.request(), virtualModelLibrary);
            JsonArray errors            = validator.validate();

            if(validator.isValid()){
                CreateFlexoConcept concept = CreateFlexoConcept.actionType.makeNewAction(model, null, editor);
                concept.setNewFlexoConceptName(validator.getName());
                concept.setVisibility(validator.getVisibility());
                concept.setIsAbstract(validator.isAbstract());
                concept.setNewFlexoConceptDescription(validator.getDescription());

                if(validator.getParent() != null) {
                    concept.addToParentConcepts(validator.getParent());
                }

                concept.doAction();

                try {
                    model.getResource().save();
                } catch (SaveResourceException e) {
                    badRequest(context);
                }

                context.response().end(JsonSerializer.conceptSerializer(concept.getNewFlexoConcept()).encodePrettily());
            } else {
                badValidation(context, errors);
            }
        } catch (FileNotFoundException | ResourceLoadingCancelledException | FlexoException e) {
            notFound(context);
        }
    }

    public void edit(RoutingContext context) {}

    /**
     * It deletes a concept from a virtual model
     *
     * @param context the routing context
     */
    public void delete(RoutingContext context) {
        String id               = context.request().getParam("id").trim();
        FlexoConcept concept    = virtualModelLibrary.getFlexoConcept(IdUtils.decodeId(id));

        if (concept != null){
            VirtualModel model                  = concept.getDeclaringCompilationUnit().getVirtualModel();
            DeleteFlexoConceptObjects action    = DeleteFlexoConceptObjects.actionType.makeNewAction(concept, null, editor);
            action.doAction();

            try {
                model.getResource().save();
            } catch (SaveResourceException e) {
                throw new RuntimeException(e);
            }
            emptyResponse(context);
        } else {
            notFound(context);
        }
    }
}
