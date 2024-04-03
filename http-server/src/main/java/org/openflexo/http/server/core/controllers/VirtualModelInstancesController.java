package org.openflexo.http.server.core.controllers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.*;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.ActionSchemeAction;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionFactory;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.http.server.core.helpers.Helpers;
import org.openflexo.http.server.core.repositories.ProjectsRepository;
import org.openflexo.http.server.core.serializers.JsonSerializer;
import org.openflexo.http.server.core.validators.VirtualModelInstancesValidator;
import org.openflexo.http.server.util.IdUtils;

import java.io.FileNotFoundException;
import java.util.List;

/**
 *  Virtual Model Instances rest apis controller.
 * @author Ihab Benamer
 */
public class VirtualModelInstancesController extends GenericController{
    private final VirtualModelLibrary virtualModelLibrary;
    private final DefaultFlexoEditor editor;

    /**
     * Instantiates a new Virtual model instances controller.
     *
     * @param virtualModelLibrary the virtual model library
     */
    public VirtualModelInstancesController(VirtualModelLibrary virtualModelLibrary) {
        this.virtualModelLibrary    = virtualModelLibrary;
        editor                      = Helpers.getDefaultFlexoEditor(virtualModelLibrary);
    }

    /**
     * It gets the project id and the virtual model id from the request, then it gets the project from the repository, then
     * it gets the virtual model instances from the project, then it serializes them to JSON and sends them back to the
     * client
     *
     * @param context the context of the request
     */
    public void list(RoutingContext context) {
        String prjid    = context.request().getParam("prjid");
        String vmid     = context.request().getParam("vmid");

        try {
            FlexoProject<?> project = ProjectsRepository.getProjectById(virtualModelLibrary, prjid);
            JsonArray result        = new JsonArray();

            for (FMLRTVirtualModelInstance vmi: project.getVirtualModelInstanceRepository().getVirtualModelInstancesConformToVirtualModel(IdUtils.decodeId(vmid))){
                result.add(JsonSerializer.virtualModelInstanceSerializer(vmi));
            }

            context.response().end(result.encodePrettily());
        } catch (Exception e) {
            notFound(context);
        }
    }

    /**
     * It gets the project id from the request, gets the project from the repository, gets the virtual model instance from
     * the project, and then serializes it to JSON
     *
     * @param context the routing context
     */
    public void get(RoutingContext context) {
        String prjid    = context.request().getParam("prjid");
        String id       = context.request().getParam("id");

        FlexoProject<?> project = ProjectsRepository.getProjectById(virtualModelLibrary, prjid);
        if (project != null) {
            FMLRTVirtualModelInstance instance = project.getVirtualModelInstanceRepository().getVirtualModelInstance(IdUtils.decodeId(id)).getVirtualModelInstance();
            context.response().end(JsonSerializer.virtualModelInstanceSerializer(instance).encodePrettily());
        } else {
            notFound(context);
        }
    }

    /**
     * It creates a new virtual model instance from a virtual model
     *
     * @param context the routing context
     */
    public void add(RoutingContext context) {
        String prjId            = context.request().getParam("prjid");
        String vmId             = context.request().getParam("vmid");
        String creationScheme   = context.request().getFormAttribute("creation_scheme");
        FlexoProject<?> project = ProjectsRepository.getProjectById(virtualModelLibrary, prjId);

        if(project != null){
            VirtualModelInstancesValidator validator    = new VirtualModelInstancesValidator(context.request(), virtualModelLibrary);
            JsonArray errors                            = validator.validate();

            if(validator.isValid()){
                try {
                    VirtualModel model                  = virtualModelLibrary.getVirtualModel(IdUtils.decodeId(vmId));
                    CreateBasicVirtualModelInstance vmi = CreateBasicVirtualModelInstance.actionType.makeNewAction(project.getVirtualModelInstanceRepository().getRootFolder(), null, editor);

                    vmi.setNewVirtualModelInstanceName(validator.getName());
                    vmi.setNewVirtualModelInstanceTitle(validator.getTitle());
                    vmi.setVirtualModel(model);

                    if (model.hasCreationScheme()){
                        CreationScheme creationBhv = null;

                        try{
                            creationBhv = model.getCreationSchemes().get(0);
                        } catch (Exception e) {
                            badRequest(context, "No creation scheme found");
                        }

                        if(creationScheme != null && !creationScheme.isEmpty()){
                            for (CreationScheme behaviour: model.getCreationSchemes()) {
                                if (behaviour.getSignature().equals(creationScheme.trim())){
                                    creationBhv = behaviour;
                                    vmi.setCreationScheme(behaviour);
                                }
                            }
                        }
                        vmi.setCreationScheme(creationBhv);

                        for(FlexoBehaviourParameter p : creationBhv.getParameters()) {
                            if(p.getType().getTypeName().equals("java.lang.String")){
                                String parameterValue = context.request().getParam(p.getName());

                                if(parameterValue == null) {
                                    JsonObject errorLine = new JsonObject();
                                    errorLine.put(p.getName(), "Missing parameter value");
                                    badValidation(context, new JsonArray().add(errorLine));
                                }
                                vmi.setParameterValue(p, parameterValue);
                            } else {
                                badRequest(context, "Only string parameters are supported");
                            }
                        }
                    }

                    vmi.doAction();

                    try {
                        model.getResource().save();
                    } catch (SaveResourceException e) {
                        badRequest(context);
                    }

                    context.response().end(JsonSerializer.virtualModelInstanceSerializer(vmi.getNewVirtualModelInstance()).encodePrettily());
                } catch (FileNotFoundException | ResourceLoadingCancelledException | FlexoException e) {
                    notFound(context);
                }
            } else {
                badValidation(context, errors);
            }
        } else {
            notFound(context);
        }
    }

    public void edit(RoutingContext context) {}

    /**
     * It deletes a virtual model instance from a project
     *
     * @param context the routing context
     */
    public void delete(RoutingContext context) {
        String prjid            = context.request().getParam("prjid");
        String id               = context.request().getParam("id");
        FlexoProject<?> project = ProjectsRepository.getProjectById(virtualModelLibrary, prjid);

        if (project != null) {
            FMLRTVirtualModelInstance instance  = project.getVirtualModelInstanceRepository().getVirtualModelInstance(IdUtils.decodeId(id)).getVirtualModelInstance();
            VirtualModel model                  = instance.getVirtualModel();

            project.getVirtualModelInstanceRepository().getVirtualModelInstancesConformToVirtualModel(model.getURI()).remove(instance);
            instance.getResource().delete();
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

    /**
     * It gets the virtual model with the given id, then iterates over all the behaviours of the virtual model, and if the
     * behaviour is an action scheme and is public, it adds it to the result array
     *
     * @param context the routing context
     */
    public void behaviours(RoutingContext context) {
        String vmid = context.request().getParam("vmid");

        try {
            VirtualModel model  = virtualModelLibrary.getVirtualModel(IdUtils.decodeId(vmid));
            JsonArray result    = new JsonArray();

            for (FlexoBehaviour behaviour : model.getFlexoBehaviours()) {
                if (behaviour instanceof ActionScheme && behaviour.getVisibility() == Visibility.Public){
                    result.add(JsonSerializer.behaviourSerializer(behaviour));
                }
            }

            context.response().end(result.encodePrettily());
        } catch (FileNotFoundException | ResourceLoadingCancelledException | FlexoException e) {
            notFound(context);
        }
    }

    /**
     * It gets the project, the virtual model instance and the behaviour from the request, then it creates an action of the
     * behaviour's type, sets the parameters and executes it
     *
     * @param context the routing context
     */
    public void executeBehaviour(RoutingContext context) {
        String prjid            = context.request().getParam("prjid");
        String vmid             = context.request().getParam("vmid");
        String id               = context.request().getParam("id");
        String signature        = context.request().getParam("signature");
        FlexoProject<?> project = ProjectsRepository.getProjectById(virtualModelLibrary, prjid);

        if (project != null) {
            FMLRTVirtualModelInstance instance  = project.getVirtualModelInstanceRepository().getVirtualModelInstance(IdUtils.decodeId(id)).getVirtualModelInstance();
            FlexoBehaviour behaviour            = virtualModelLibrary.getFlexoConcept(IdUtils.decodeId(vmid)).getDeclaredFlexoBehaviour(signature);

            if(behaviour instanceof ActionScheme && behaviour.getVisibility() == Visibility.Public) {
                ActionScheme actionScheme               = (ActionScheme) behaviour;
                ActionSchemeActionFactory actionType    = new ActionSchemeActionFactory(actionScheme, instance.getFlexoConceptInstance());
                ActionSchemeAction action               = actionType.makeNewAction(instance.getVirtualModelInstance(), null, editor);

                for(FlexoBehaviourParameter p : actionScheme.getParameters()) {
                    if(p.getType().getTypeName().equals("java.lang.String")){
                        String parameterValue = context.request().getParam((p.getName()));
                        if(parameterValue == null) {
                            JsonObject errorLine = new JsonObject();
                            errorLine.put(p.getName(), "Missing parameter value");
                            badValidation(context, new JsonArray().add(errorLine));
                        }
                        action.setParameterValue(p, parameterValue);
                    } else {
                        badRequest(context, "Only string parameters are supported");
                    }
                }
                action.doAction();

                try {
                    instance.getResource().save();
                } catch (SaveResourceException e) {
                    badRequest(context);
                }
                emptyResponse(context);
            } else {
                badRequest(context, "Invalid behaviour's type or visibility");
            }
        } else {
            notFound(context);
        }
    }

}
