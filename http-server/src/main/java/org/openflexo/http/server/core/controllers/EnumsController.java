package org.openflexo.http.server.core.controllers;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.*;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoEnum;
import org.openflexo.foundation.fml.action.CreateFlexoEnumValue;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.http.server.core.helpers.Helpers;
import org.openflexo.http.server.core.serializers.JsonSerializer;
import org.openflexo.http.server.core.validators.ConceptsValidator;
import org.openflexo.http.server.core.validators.EnumsValidator;
import org.openflexo.http.server.util.IdUtils;

import java.io.FileNotFoundException;

/**
 *  Enums rest apis controller.
 * @author Ihab Benamer
 */
public class EnumsController extends GenericController {
    private final VirtualModelLibrary virtualModelLibrary;
    private final DefaultFlexoEditor editor;

    /**
     * Instantiates a new Enums controller.
     *
     * @param virtualModelLibrary the virtual model library
     */
    public EnumsController(VirtualModelLibrary virtualModelLibrary) {
        this.virtualModelLibrary    = virtualModelLibrary;
        editor                      = Helpers.getDefaultFlexoEditor(virtualModelLibrary);
    }

    /**
     * It gets the virtual model from the library, then iterates over all the flexo concepts of the virtual model, and if
     * the flexo concept is an enum, it adds it to the result array
     *
     * @param context the context of the request
     */
    public void list(RoutingContext context) {
        String id = context.request().getParam("vmid").trim();
        try {
            VirtualModel model  = virtualModelLibrary.getVirtualModel(IdUtils.decodeId(id));
            JsonArray result    = new JsonArray();

            for (FlexoConcept fEnum : model.getFlexoConcepts()) {
                if(fEnum instanceof FlexoEnum){
                    result.add(JsonSerializer.enumSerializer(fEnum));
                }
            }

            context.response().end(result.encodePrettily());
        } catch (Exception e) {
            notFound(context);
        }
    }


    /**
     * Get the enum with the given id and return it as a JSON object
     *
     * @param context the routing context
     */
    public void get(RoutingContext context) {
        String id               = context.request().getParam("id").trim();
        FlexoConcept flexoEnum  = virtualModelLibrary.getFlexoConcept(IdUtils.decodeId(id));

        if (flexoEnum instanceof FlexoEnum){
            context.response().end(JsonSerializer.enumSerializer(flexoEnum).encodePrettily());
        } else {
            notFound(context);
        }
    }

    /**
     * It creates a new FlexoEnum in the VirtualModel
     *
     * @param context the routing context
     */
    public void add(RoutingContext context) {
        String id = context.request().getParam("vmid");

        try {
            VirtualModel model          = virtualModelLibrary.getVirtualModel(IdUtils.decodeId(id));
            EnumsValidator validator    = new EnumsValidator(context.request());
            JsonArray errors            = validator.validate();

            if(validator.isValid()){
                CreateFlexoEnum flexoEnum = CreateFlexoEnum.actionType.makeNewAction(model, null, editor);

                flexoEnum.setNewFlexoEnumName(validator.getName());
//                flexoEnum.setNewFlexoEnumDescription(validator.getDescription());
                flexoEnum.setVisibility(validator.getVisibility());
                flexoEnum.setIsAbstract(validator.isAbstract());
                flexoEnum.doAction();

                try {
                    model.getResource().save();
                } catch (SaveResourceException e) {
                    badRequest(context);
                }

                context.response().end(JsonSerializer.enumSerializer(flexoEnum.getNewFlexoConcept()).encodePrettily());
            } else {
                badValidation(context, errors);
            }
        } catch (FileNotFoundException | ResourceLoadingCancelledException | FlexoException e) {
            notFound(context);
        }
    }

    /**
     * It gets the id of the enum from the request, gets the enum from the virtual model library, creates a JsonArray, adds
     * the serialized values of the enum to the JsonArray, and then sends the JsonArray as a response
     *
     * @param context the context of the request
     */
    public void values(RoutingContext context) {
        String id           = context.request().getParam("id").trim();
        FlexoEnum flexoEnum = (FlexoEnum) virtualModelLibrary.getFlexoConcept(IdUtils.decodeId(id));
        JsonArray result    = new JsonArray();

        if (flexoEnum != null){
            for (FlexoEnumValue value : flexoEnum.getValues()) {
                result.add(JsonSerializer.enumValueSerializer(value));
            }

            context.response().end(result.encodePrettily());
        } else {
            notFound(context);
        }
    }

    /**
     * It creates a new value for a given FlexoEnum
     *
     * @param context the routing context
     */
    public void addValue(RoutingContext context) {
        String id                   = context.request().getParam("id").trim();
        FlexoEnum flexoEnum         = (FlexoEnum) virtualModelLibrary.getFlexoConcept(IdUtils.decodeId(id));
        EnumsValidator validator    = new EnumsValidator(context.request());
        JsonArray errors            = validator.validateValue();

        if(validator.isValid()){
            CreateFlexoEnumValue value = CreateFlexoEnumValue.actionType.makeNewAction(flexoEnum, null, editor);
            value.setValueName(validator.getName());
            value.setDescription(validator.getDescription());
            value.doAction();

            try {
                flexoEnum.getDeclaringCompilationUnit().getVirtualModel().getResource().save();
            } catch (SaveResourceException e) {
                badRequest(context);
            }

            context.response().end(JsonSerializer.enumValueSerializer(value.getNewValue()).encodePrettily());
        } else {
            badValidation(context, errors);
        }
    }

    /**
     * It deletes a FlexoEnum from a VirtualModel
     *
     * @param context the routing context
     */
    public void delete(RoutingContext context) {
        String id               = context.request().getParam("id").trim();
        FlexoConcept flexoEnum  = virtualModelLibrary.getFlexoConcept(IdUtils.decodeId(id));

        if (flexoEnum instanceof FlexoEnum){
            VirtualModel model = flexoEnum.getDeclaringCompilationUnit().getVirtualModel();
            flexoEnum.delete();

            try {
                model.getResource().save();
            } catch (SaveResourceException e) {
                badRequest(context);
            }

            emptyResponse(context);
        } else {
            notFound(context);
        }
    }
}
