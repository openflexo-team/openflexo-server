package org.openflexo.http.server.core.serializers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.*;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.logging.FMLLogRecord;
import org.openflexo.foundation.resource.*;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResourceRepository;
import org.openflexo.http.server.core.RestTerminal;
import org.openflexo.http.server.core.TechnologyAdapterRouteComplement;
import org.openflexo.http.server.core.TechnologyAdapterRouteService;
import org.openflexo.http.server.util.IdUtils;
import org.openflexo.http.server.util.ResourceRestService;
import org.openflexo.http.server.util.ResourceUtils;

import java.nio.file.Path;
import java.util.*;

/**
 * A class for defining Json serializers
 *
 * @author Ihab Benamer
 */
public class JsonSerializer {

    /** Map of technology adapters to corresponding {@link TechnologyAdapterRouteComplement} */
    private final static Map<TechnologyAdapter<?>, TechnologyAdapterRouteComplement> complementMap = new LinkedHashMap<>();
    /** Map of registered {@link org.openflexo.http.server.util.PamelaResourceRestService}s for each technology adapters */
    private final static Map<TechnologyAdapter<?>, List<ResourceRestService>> restServices = new HashMap<>();

    /**
     * It takes a FlexoProject and returns a JsonObject that contains the project's name, uri, id, and resource center id
     *
     * @param project the project to serialize
     * @return A JsonObject
     */
    public static JsonObject projectSerializer(FlexoProject<?> project) {
        String uri          = project.getProjectURI();
        String id           = IdUtils.encodeuri(uri);
        JsonObject result   = new JsonObject();

        result.put("name", project.getDisplayableName());
        result.put("resource_type", "Project");
        result.put("uri", uri);
        result.put("id", id);
        result.put("resource_center_id", IdUtils.encodeuri(project.getResourceCenter().getDefaultBaseURI()));

        return result;
    }

    /**
     * It takes a VirtualModel object and returns a JsonObject that contains the name, resource_type, uri, id, visibility,
     * and is_abstract fields
     *
     * @param virtualModel The VirtualModel object that you want to serialize.
     * @return A JsonObject
     */
    public static JsonObject virtualModelSerializer(VirtualModel virtualModel) {
        String uri          = virtualModel.getURI();
        String id           = IdUtils.encodeuri(uri);
        JsonObject result   = new JsonObject();

        String parent_id;

        try{
            parent_id = IdUtils.encodeuri(virtualModel.getParentFlexoConcepts().get(0).getURI());
        } catch (NullPointerException | IndexOutOfBoundsException e)  {
            parent_id = null;
        }

        result.put("name", virtualModel.getName());
        result.put("resource_type", "VirtualModel");
        result.put("uri", uri);
        result.put("id", id);
        result.put("visibility", virtualModel.getVisibility());
        result.put("is_abstract", virtualModel.isAbstract());
        result.put("parent_id", parent_id);

        return result;
    }

    /**
     * It takes a PrimitiveRole object and returns a JsonObject
     *
     * @param role the role to serialize
     * @return A JsonObject
     */
    public static JsonObject primitivePropertySerializer(PrimitiveRole<?> role) {
        JsonObject result = new JsonObject();

        result.put("name", role.getRoleName());
        result.put("resource_type", "PrimitiveProperty");
        result.put("cardinality", role.getCardinality());
        result.put("type", role.getPrimitiveType());
        result.put("virtual_model_id", IdUtils.encodeuri(role.getDeclaringCompilationUnit().getVirtualModel().getURI()));

        return result;
    }

    /**
     * It takes a FlexoBehaviour object and returns a JsonObject that contains all the information that we want to send to
     * the client
     *
     * @param behaviour the behaviour to serialize
     * @return A JsonObject
     */
    public static JsonObject behaviourSerializer(FlexoBehaviour behaviour){
        JsonObject result = new JsonObject();

        result.put("name", behaviour.getName());
        result.put("resource_type", "Behaviour");
        result.put("id", IdUtils.encodeuri(behaviour.getURI()));
        result.put("type", behaviour.getFlexoBehaviourType().getTypeName());
        result.put("visibility", behaviour.getVisibility());
        result.put("is_abstract", behaviour.isAbstract());
        result.put("description", behaviour.getDescription());
        result.put("signature", behaviour.getSignature());
        result.put("virtual_model_id", IdUtils.encodeuri(behaviour.getDeclaringCompilationUnit().getVirtualModel().getURI()));

        return result;
    }

    /**
     * It takes a FlexoBehaviourParameter object and returns a JsonObject that contains all the information about the
     * parameter
     *
     * @param parameter the parameter to serialize
     * @return A JsonObject
     */
    public static JsonObject behaviourParameterSerializer(FlexoBehaviourParameter parameter){
        JsonObject result = new JsonObject();

        result.put("name", parameter.getName());
        result.put("resource_type", "BehaviourParameter");
        result.put("type", parameter.getType().getTypeName());
        result.put("default_value", parameter.getDefaultValue().toString());
        result.put("is_required", parameter.getIsRequired());
        result.put("description", parameter.getDescription());
        result.put("behaviour_id", IdUtils.encodeuri(parameter.getBehaviour().getURI()));
        result.put("behaviour_signature", parameter.getBehaviour().getSignature());

        return result;
    }

    /**
     * It takes a FlexoConcept and returns a JsonObject
     *
     * @param concept the concept to serialize
     * @return A JsonObject
     */
    public static JsonObject conceptSerializer(FlexoConcept concept) {
        JsonObject result = new JsonObject();
        String parent_id;

        try{
            parent_id = IdUtils.encodeuri(concept.getParentFlexoConcepts().get(0).getURI());
        } catch (NullPointerException | IndexOutOfBoundsException e)  {
            parent_id = null;
        }

        result.put("id", IdUtils.encodeuri(concept.getURI()));
        result.put("name", concept.getName());
        result.put("uri", concept.getURI());
        result.put("resource_type", "Concept");
        result.put("visibility", concept.getVisibility().toString());
        result.put("is_abstract", concept.isAbstract());
        result.put("description", concept.getDescription());
        result.put("parent_id", parent_id);
        result.put("virtual_model_id", IdUtils.encodeuri(concept.getDeclaringCompilationUnit().getVirtualModel().getURI()));

        return result;
    }

    /**
     * It takes a VirtualModelInstance object and returns a JsonObject that contains the name, uri, resource_type, title,
     * and virtual_model_uri of the VirtualModelInstance
     *
     * @param instance the VirtualModelInstance object to serialize
     * @return A JsonObject
     */
    public static JsonObject virtualModelInstanceSerializer(VirtualModelInstance instance) {
        JsonObject result           = new JsonObject();

        result.put("name", instance.getName());
        result.put("uri", instance.getURI());
        result.put("id", IdUtils.encodeuri(instance.getURI()));
        result.put("resource_type", "VirtualModelInstance");
        result.put("title", instance.getTitle());
        result.put("virtual_model_id", IdUtils.encodeuri(instance.getVirtualModel().getURI()));

        return result;
    }

    /**
     * It takes a FlexoConceptInstance and returns a JsonObject that contains the FlexoConceptInstance's flexoID, resource
     * type, and concept URI
     *
     * @param instance the instance of the concept to serialize
     * @return A JsonObject
     */
    public static JsonObject conceptInstanceSerializer(FlexoConceptInstance instance) {
        JsonObject result           = new JsonObject();

        result.put("flexo_id", instance.getFlexoID());
        result.put("resource_type", "FlexoConceptInstance");
        result.put("concept_id", IdUtils.encodeuri(instance.getFlexoConceptURI()));

        return result;
    }

    public static JsonObject getCenterDescription(FlexoResourceCenter<?> center) {
        String uri = center.getDefaultBaseURI();
        String id = IdUtils.encodeuri(uri);
        JsonObject centerDescription = new JsonObject();
        centerDescription.put("name", center.getDisplayableName());
        centerDescription.put("type", "ResourceCenter");
        centerDescription.put("uri", uri);
        centerDescription.put("id", id);
        centerDescription.put("url", "/rc/" + id);
        centerDescription.put("resourceUrl", "/rc/" + id + "/resource");
        return centerDescription;
    }

    public static JsonObject getRepositoryDescription(ResourceRepository<?, ?> repository) {
        String uri = repository.getDefaultBaseURI();
        String id = IdUtils.encodeuri(uri);
        JsonObject description = new JsonObject();
        description.put("name", repository.getDisplayableName());
        description.put("type", "ResourceRepository");
        description.put("uri", uri);
        description.put("id", id);
        description.put("url", "/rc/" + id);
        description.put("resourceUrl", "/rc/" + id + "/resource");
        if (repository instanceof TechnologyAdapterResourceRepository) {
            String taId = IdUtils
                    .getTechnologyAdapterId(((TechnologyAdapterResourceRepository<?, ?, ?, ?>) repository).getTechnologyAdapter());
            description.put("technologyAdapterId", taId);
            description.put("technologyAdapterUrl", "/ta/" + taId);
        }
        return description;

    }

    public static JsonObject getFolderDescription(String name, String path, String rcId) {
        JsonObject folderDescription = new JsonObject();
        folderDescription.put("name", name);
        folderDescription.put("type", "Folder");
        String id = path + (path.endsWith("/") ? "" : "/") + name;
        folderDescription.put("id", id);

        String rcUrl = "/rc/" + rcId;
        folderDescription.put("url", rcUrl + "/resource" + id);
        folderDescription.put("resourceCenterId", rcId);
        folderDescription.put("resourceCenterUrl", rcUrl);
        return folderDescription;
    }

    public static JsonObject getResourceDescription(FlexoResource<?> resource, TechnologyAdapterRouteService service) {
        String uri = resource.getURI();
        String id = IdUtils.encodeuri(uri);
        JsonObject resourceDescription = new JsonObject();
        resourceDescription.put("name", resource.getName());
        resourceDescription.put("type", "Resource");
        resourceDescription.put("uri", uri);
        resourceDescription.put("id", id);
        resourceDescription.put("modified", resource.isModified());
        resourceDescription.put("loaded", resource.isLoaded());
        resourceDescription.put("hash", resource.hash());

        if (resource.getResourceCenter() != null) {
            String centerId = IdUtils.encodeuri(resource.getResourceCenter().getDefaultBaseURI());
            resourceDescription.put("resourceCenterId", centerId);
            resourceDescription.put("resourceCenterUrl", "/rc/" + centerId);
        }
        else {
            System.out.println("Resource '" + resource.getName() + "' has no rc.");
        }
        resourceDescription.put("url", "/resource/" + id);
        resourceDescription.put("contentUrl", "/resource/" + id + "/contents");
        if (resource instanceof TechnologyAdapterResource) {
            TechnologyAdapterResource<?, ?> technologyAdapterResource = (TechnologyAdapterResource<?, ?>) resource;
            TechnologyAdapter<?> technologyAdapter = technologyAdapterResource.getTechnologyAdapter();
            String taId = IdUtils.getTechnologyAdapterId(technologyAdapter);
            resourceDescription.put("technologyAdapterId", taId);
            resourceDescription.put("technologyAdapterUrl", "/ta/" + taId);

            String prefix = service.getPrefix(resource);
            if (prefix != null) {
                if (resource.isLoaded()) {
                    ResourceData<?> data = ResourceUtils.safeGetResourceOrNull(resource);
                    if (data instanceof FlexoObject) {
                        String url = IdUtils.getUrl(data, service);
                        if (url != null) {
                            resourceDescription.put("modelUrl", url);
                        }
                    }
                }
                resourceDescription.put("objectsUrl", prefix + "/" + id + "/object");
            }
        }
        return resourceDescription;
    }

    public static JsonObject getTechnologyAdapterDescription(String id, TechnologyAdapter<?> adapter, TechnologyAdapterRouteService service) {
        JsonObject result = new JsonObject();
        result.put("name", adapter.getName());
        result.put("type", "TechnologyAdapter");

        result.put("id", id);
        result.put("activated", adapter.isActivated());

        String url = "/ta/" + id;
        result.put("url", url);

        service.complementTechnologyAdapter(adapter, result);

        return result;
    }

    /**
     * It takes an EditionAction and returns a JSON object that contains the action's name, description, and the ID of the
     * behaviour that owns it
     *
     * @param action The action to be serialized
     * @return A JsonObject
     */
    public static JsonObject behaviourActionSerializer(EditionAction action){
        JsonObject result   = new JsonObject();
        String behaviourId  = null;

        result.put("name", action.getName());
        result.put("resource_type", "BehaviourAction");
        result.put("description", action.getDescription());

        if(action.getRootOwner() instanceof FlexoBehaviour){
            behaviourId  = IdUtils.encodeuri(((FlexoBehaviour) action.getRootOwner()).getURI());
        }
        // TODO : implement the else bloc
        result.put("behaviour_id", behaviourId);

        return result;
    }

    /**
     * It takes a log record and returns a JSON object
     *
     * @param record the log record to serialize
     * @return A JsonObject
     */
    public  static  JsonObject logRecordSerializer(FMLLogRecord record){
        JsonObject result = new JsonObject();

        result.put("level", record.level.toString());
        result.put("value", record.message);
        result.put("date", record.date.toString());
        result.put("millis", record.millis);
        result.put("concept_id", IdUtils.encodeuri(record.flexoConceptInstance.getFlexoConceptURI()));

        return result;
    }

    /**
     * It takes a FlexoConceptInstanceRole and returns a JsonObject that contains the information needed to recreate the
     * role
     *
     * @param role the role to serialize
     * @return A JsonObject
     */
    public static JsonObject conceptInstanceRoleSerializer(FlexoConceptInstanceRole role) {
        JsonObject result = new JsonObject();

        result.put("name", role.getRoleName());
        result.put("resource_type", "ConceptInstanceRole");
        result.put("cardinality", role.getCardinality());
        result.put("description", role.getDescription());
        result.put("concept_type_name", role.getFlexoConceptType().getName());
        result.put("concept_type_id", IdUtils.encodeuri(role.getFlexoConceptType().getURI()));
        result.put("concept_id", IdUtils.encodeuri(role.getFlexoConcept().getURI()));

        return result;
    }

    /**
     * It takes a model slot and returns a JSON object that contains all the information needed to recreate the model slot
     *
     * @param modelSlot the model slot to serialize
     * @return A JsonObject
     */
    public static JsonObject modelSlotSerializer(FMLRTModelSlot<?, ?> modelSlot) {
        JsonObject result = new JsonObject();

        result.put("name", modelSlot.getName());
        result.put("resource_type", "ModelSlot");
        result.put("technology_adapter", modelSlot.getTechnologyAdapter().getName());
        result.put("read_only", modelSlot.getIsReadOnly());
        result.put("required", modelSlot.getIsRequired());
        result.put("description", modelSlot.getDescription());
        result.put("virtual_model_id", IdUtils.encodeuri(modelSlot.getFlexoConcept().getURI()));

        return result;
    }

    /**
     * It takes a FlexoConcept as input and returns a JsonObject that contains the information we want to send to the
     * client
     *
     * @param concept the FlexoConcept to serialize
     * @return A JsonObject
     */
    public static JsonObject enumSerializer(FlexoConcept concept) {
        JsonObject result           = new JsonObject();

        result.put("id", IdUtils.encodeuri(concept.getURI()));
        result.put("name", concept.getName());
        result.put("uri", concept.getURI());
        result.put("resource_type", "FlexoEnum");
        result.put("visibility", concept.getVisibility().toString());
        result.put("is_abstract", concept.isAbstract());
        result.put("description", concept.getDescription());
        result.put("virtual_model_id", IdUtils.encodeuri(concept.getDeclaringCompilationUnit().getVirtualModel().getURI()));

        return result;
    }

    /**
     * It takes a FlexoEnumValue and returns a JsonObject
     *
     * @param value the FlexoEnumValue to serialize
     * @return A JsonObject
     */
    public static JsonObject enumValueSerializer(FlexoEnumValue value){
        JsonObject result = new JsonObject();

        result.put("id", IdUtils.encodeuri(value.getURI()));
        result.put("name", value.getName());
        result.put("uri", value.getURI());
        result.put("resource_type", "FlexoEnum");
        result.put("index", value.getIndex());
        result.put("enum_id", IdUtils.encodeuri(value.getFlexoConcept().getURI()));
        result.put("description", value.getDescription());

        return result;
    }

    /**
     * It takes a folder object and returns a JSON object with the folder's name, resource type, parent folder,
     * modification status, URI, and ID
     *
     * @param folder The folder object to be serialized.
     * @return A JsonObject
     */
    public static JsonObject repositoryFolderSerializer(RepositoryFolder folder) {
        JsonObject result = new JsonObject();

        result.put("name", folder.getName());
        result.put("resource_type", "Folder");
        result.put("parent", folder.getParentFolder().getName());
        result.put("modified", folder.isModified());
        result.put("uri", folder.getDefaultBaseURI());
        result.put("id", IdUtils.encodeuri(folder.getDefaultBaseURI()));

        return result;
    }

    /**
     * It takes a list of strings and a terminal object and returns a JSON object
     *
     * @param output The output of the command
     * @param terminal The terminal object that was created when the user logged in.
     * @return A JsonObject
     */
    public static JsonObject terminalOutputSerializer(List<String> output, RestTerminal terminal) {
        JsonArray out   = new JsonArray();
        JsonObject term = new JsonObject();
        JsonObject line;

        if (output != null){
            for (String l: output) {
                line = new JsonObject();
                line.put("line", l.replace(">", "]").replace("<", "[").trim());
                line.put("resource_type", "CommandLineOutput");
                out.add(line);
            }
        }

        term.put("prompt", terminal.getCi().getPrompt());
        term.put("output", out);

        return term;
    }

    /**
     * It takes a list of commands and returns a JSON array of objects that represent the commands
     *
     * @param history The list of commands that you want to display.
     * @return A JsonArray
     */
    public static JsonArray terminalHistorySerializer(List<AbstractCommand> history) {
        JsonArray result = new  JsonArray();

        for (AbstractCommand cmd : history){
            JsonObject line = new JsonObject();

            line.put("text", cmd.toString());
            line.put("replace", true);
            line.put("type", "input");

            result.add(line);
        }

        return result;
    }

    /**
     * This Java function serializes a list of strings into a JSON array.
     *
     * @param values values is a List of Strings that contains the possible completion options for a terminal command. The
     * method terminalCompletionSerializer takes this list as input and returns a JsonArray that contains the same values.
     * This is useful for sending the completion options to a client application that is interacting with the terminal.
     * @return A JsonArray containing the values from the input List<String> is being returned.
     */
    public static JsonArray terminalCompletionSerializer(List<String> values) {
        JsonArray result = new JsonArray();

        for (String val : values) {
            result.add(val);
        }

        return result;
    }
}
