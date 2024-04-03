package org.openflexo.http.server.core.validators;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.*;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.http.server.core.exceptions.BadValidationException;
import org.openflexo.http.server.core.repositories.ProjectsRepository;
import org.openflexo.http.server.util.IdUtils;

/**
 * The Concept Instances validator class.
 *
 * @author Ihab Benamer
 */
public class ConceptInstancesValidator extends GenericValidator{
    private final VirtualModelLibrary virtualModelLibrary;
    private final HttpServerRequest request;
    private boolean isValid;
    private JsonArray errors;
    private FlexoConcept concept;
    private FMLRTVirtualModelInstance container;

    /**
     * Instantiates a new Concept instance validator.
     *
     * @param request             the request
     * @param virtualModelLibrary the virtual model library
     */
    public ConceptInstancesValidator(HttpServerRequest request, VirtualModelLibrary virtualModelLibrary) {
        this.virtualModelLibrary    = virtualModelLibrary;
        this.request                = request;
        isValid                     = false;
    }

    /**
     * It validates a project id and returns the corresponding project
     *
     * @param projectId the name of the parameter that will be passed to the method.
     * @return A FlexoProject
     */
    public FlexoProject<?> validateProject(String projectId) throws BadValidationException {
        if(projectId == null || projectId.isEmpty()){
            throw new BadValidationException("Field required");
        } else {
            FlexoProject<?> project = ProjectsRepository.getProjectById(virtualModelLibrary, projectId);

            if (project != null) {
                return project;
            } else {
                throw new BadValidationException("Invalid value");
            }
        }
    }


    /**
     * It takes a string as input, and returns a virtual model instance. If the string is empty, it throws an exception. If
     * the string is not empty, it tries to find the virtual model instance with the given id. If it finds it, it returns
     * it. If it doesn't find it, it throws an exception
     *
     * @param vmiId the id of the virtual model instance to validate
     * @param project the project in which the virtual model instance is located
     * @return A VirtualModelInstance
     */
    public FMLRTVirtualModelInstance validateVirtualModelInstanceId(String vmiId, FlexoProject<?> project) throws BadValidationException {
        if(vmiId == null || vmiId.isEmpty()){
            throw new BadValidationException("Field required");
        } else {
            FMLRTVirtualModelInstance vmi = project.getVirtualModelInstanceRepository().getVirtualModelInstance(IdUtils.decodeId(vmiId)).getVirtualModelInstance();
            if (vmi != null) {
                return vmi;
            } else {
                throw new BadValidationException("Invalid value");
            }
        }
    }

    /**
     * It validates the parameters of the request and returns a JsonArray of errors
     *
     * @return A JsonArray of JsonObjects. Each JsonObject contains a key/value pair. The key is the name of the parameter
     * that failed validation. The value is the error message.
     */
    public JsonArray validate(){
        String rConceptId       = request.getFormAttribute("concept_id");
        String rProjectId       = request.getFormAttribute("project_id");
        String rContainerId     = request.getFormAttribute("container_id");
        errors                  = new JsonArray();

        JsonObject errorLine;

        try{
            concept = validateConcept(rConceptId, virtualModelLibrary);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("concept_uri", e.getMessage());
            errors.add(errorLine);
        }

        try{
            FlexoProject<?> project = validateProject(rProjectId);
            try{
                container = validateVirtualModelInstanceId(rContainerId, project);
            } catch (BadValidationException e){
                errorLine = new JsonObject();
                errorLine.put("container_uri", e.getMessage());
                errors.add(errorLine);
            }
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("project_id", e.getMessage());
            errors.add(errorLine);
        }

        if(errors.isEmpty())
            isValid = true;

        return errors;
    }

    /**
     * This method returns true if the object is valid, and false otherwise.
     *
     * @return The boolean value of isValid.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * This method returns the FlexoConcept of this object
     *
     * @return The concept of the current object.
     */
    public FlexoConcept getConcept() {
        return concept;
    }

    /**
     * This method returns the FMLRTVirtualModelInstance of this object
     *
     * @return The container of the virtual model instance.
     */
    public FMLRTVirtualModelInstance getContainer() {
        return container;
    }
}
