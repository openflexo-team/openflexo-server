package org.openflexo.http.server.core.controllers;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.*;
import org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole;
import org.openflexo.foundation.fml.action.CreateModelSlot;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.action.DeleteFlexoConceptObjects;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.http.server.core.helpers.Helpers;
import org.openflexo.http.server.core.serializers.JsonSerializer;
import org.openflexo.http.server.core.validators.PropertiesValidator;
import org.openflexo.http.server.util.IdUtils;

import java.io.FileNotFoundException;

/**
 *  Properties rest apis controller.
 * @author Ihab Benamer
 */
public class PropertiesController extends GenericController {
    private final VirtualModelLibrary virtualModelLibrary;
    private final DefaultFlexoEditor editor;

    /**
     * Instantiates a new Properties controller.
     *
     * @param virtualModelLibrary the virtual model library
     */
    public PropertiesController(VirtualModelLibrary virtualModelLibrary) {
        this.virtualModelLibrary    = virtualModelLibrary;
        editor                      = Helpers.getDefaultFlexoEditor(virtualModelLibrary);
    }

    /**
     * It gets the virtual model from the library, then iterates over its properties and serializes them to JSON
     *
     * @param context the routing context
     */
    public void list(RoutingContext context) {
        String id = context.request().getParam("id").trim();

        try {
            VirtualModel model  = virtualModelLibrary.getVirtualModel(IdUtils.decodeId(id));
            JsonArray result    = new JsonArray();

            for (FlexoProperty<?> property : model.getDeclaredRoles()) {
                result.add(JsonSerializer.conceptInstanceRoleSerializer((FlexoConceptInstanceRole) property));
            }

            for (FlexoProperty<?> property : model.getModelSlots()) {
                result.add(JsonSerializer.modelSlotSerializer((FMLRTModelSlot<?, ?>) property));
            }

            for (FlexoProperty<?> property : model.getDeclaredProperties()) {
                result.add(JsonSerializer.primitivePropertySerializer((PrimitiveRole<?>) property));
            }

            context.response().end(result.encodePrettily());
        } catch (Exception e) {
            notFound(context);
        }
    }

    /**
     * It gets the virtual model with the given id, gets the primitive property with the given name, and returns the value
     * of that property
     *
     * @param context The routing context is the object that contains all the information about the current HTTP request
     * and response.
     */
    public void get(RoutingContext context) {
        String id   = context.request().getParam("id").trim();
        String name = context.request().getParam("name").trim();

        try {
            VirtualModel model  = virtualModelLibrary.getVirtualModel(IdUtils.decodeId(id));

            context.response().end(JsonSerializer.primitivePropertySerializer((PrimitiveRole<?>) model.getAccessibleProperty(name)).encodePrettily());
        } catch (Exception e) {
            notFound(context);
        }
    }

    /**
     * It creates a new primitive property in the virtual model with the given id
     *
     * @param context the routing context
     */
    public void addPrimitive(RoutingContext context) {
        String id                   = context.request().getParam("id").trim();
        FlexoConcept concept        = virtualModelLibrary.getFlexoConcept(IdUtils.decodeId(id));
        PropertiesValidator validator = new PropertiesValidator(context.request(), virtualModelLibrary);
        JsonArray errors            = validator.validatePrimitive();

        if(validator.isValid()){
            CreatePrimitiveRole property = CreatePrimitiveRole.actionType.makeNewAction(concept, null, editor);

            property.setRoleName(validator.getName());
            property.setPrimitiveType(validator.getType());
            property.setCardinality(validator.getCardinality());
            property.setDescription(validator.getDescription());
            property.doAction();

            try {
                concept.getDeclaringCompilationUnit().getVirtualModel().getResource().save();
            } catch (SaveResourceException e) {
                badRequest(context);
            }

            PrimitiveRole<?> prop = property.getNewFlexoRole();

            context.response().end(JsonSerializer.primitivePropertySerializer(prop).encodePrettily());
        } else {
            badValidation(context, errors);
        }
    }

    public void edit(RoutingContext context) {}

    /**
     * It deletes a property from a virtual model
     *
     * @param context the routing context
     */
    public void delete(RoutingContext context) {

        String id   = context.request().getParam("id").trim();
        String name = context.request().getParam("name").trim();

        try {
            VirtualModel model      = virtualModelLibrary.getVirtualModel(IdUtils.decodeId(id));
            PrimitiveRole<?> prop   = (PrimitiveRole<?>) model.getAccessibleProperty(name);

            prop.getFlexoConcept().removeFromFlexoProperties(prop);
            prop.destroy();

            model.getResource().save();

            emptyResponse(context);
        } catch (FileNotFoundException | ResourceLoadingCancelledException | FlexoException e) {
            notFound(context);
        }
    }

    /**
     * It creates a new FlexoConceptInstanceRole in the FlexoConcept identified by the id parameter
     *
     * @param context the routing context
     */
    public void addFlexoConceptInstanceRole(RoutingContext context) {
        String id                       = context.request().getParam("id").trim();
        FlexoConcept concept            = virtualModelLibrary.getFlexoConcept(IdUtils.decodeId(id));
        PropertiesValidator validator   = new PropertiesValidator(context.request(), virtualModelLibrary);
        JsonArray errors                = validator.validateConceptInstanceRole();

        if(validator.isValid()){
            CreateFlexoConceptInstanceRole role = CreateFlexoConceptInstanceRole.actionType.makeNewAction(concept, null, editor);

            role.setRoleName(validator.getName());
            role.setFlexoConceptInstanceType(validator.getConcept());
            role.setCardinality(validator.getCardinality());
            role.setDescription(validator.getDescription());
            role.doAction();

            try {
                concept.getDeclaringCompilationUnit().getVirtualModel().getResource().save();
            } catch (SaveResourceException e) {
                badRequest(context);
            }

            context.response().end(JsonSerializer.conceptInstanceRoleSerializer(role.getNewFlexoRole()).encodePrettily());
        } else {
            badValidation(context, errors);
        }
    }

    /**
     * It creates a new model slot in the FlexoConcept identified by the id parameter
     *
     * @param context the routing context
     */
    public void addModelSlot(RoutingContext context) {
        String id                       = context.request().getParam("id").trim();
        FlexoConcept concept            = virtualModelLibrary.getFlexoConcept(IdUtils.decodeId(id));
        PropertiesValidator validator   = new PropertiesValidator(context.request(), virtualModelLibrary);
        JsonArray errors                = validator.validateModelSlot();

        if(validator.isValid()){
            CreateModelSlot modelSlot   = CreateModelSlot.actionType.makeNewAction(concept, null, editor);

            modelSlot.setModelSlotClass(FMLRTVirtualModelInstanceModelSlot.class);
            modelSlot.setTechnologyAdapter(validator.getTechnologyAdapter());
            modelSlot.setModelSlotName(validator.getName());
            modelSlot.setDescription(validator.getDescription());
            modelSlot.setReadOnly(validator.isReadOnly());
            modelSlot.setRequired(validator.isRequired());
            modelSlot.setVmRes(validator.getVirtualModelResource());
            modelSlot.doAction();

            VirtualModel model = concept.getDeclaringCompilationUnit().getVirtualModel();
            try {
                model.getResource().save();
            } catch (SaveResourceException e) {
                badRequest(context);
            }

            context.response().end(JsonSerializer.modelSlotSerializer((FMLRTModelSlot<?, ?>) modelSlot.getNewModelSlot()).encodePrettily());
        } else {
            badValidation(context, errors);
        }
    }
}
