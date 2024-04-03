package org.openflexo.http.server.core.controllers;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.fml.*;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
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

    public void list(RoutingContext context) {}

    public void get(RoutingContext context) {}

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
