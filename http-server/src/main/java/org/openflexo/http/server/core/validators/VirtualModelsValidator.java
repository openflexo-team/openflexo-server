package org.openflexo.http.server.core.validators;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.http.server.core.exceptions.BadValidationException;
import org.openflexo.http.server.core.repositories.ProjectsRepository;

/**
 * The Virtual Models validator class.
 *
 * @author Ihab Benamer
 */
public class VirtualModelsValidator extends GenericValidator{

    private final VirtualModelLibrary virtualModelLibrary;
    private final HttpServerRequest request;
    private boolean isValid;
    private JsonArray errors;
    private String name;
    private String description;
    private Visibility visibility;
    private boolean isAbstract;
    private String projectId;
    private String path;
    private FlexoConcept parent;
    private String fml;

    /**
     * Instantiates a new Virtual models validator.
     *
     * @param request             the request
     * @param virtualModelLibrary the virtual model library
     */
    public VirtualModelsValidator(HttpServerRequest request, VirtualModelLibrary virtualModelLibrary){
        this.request                = request;
        this.virtualModelLibrary    = virtualModelLibrary;
        isValid                     = false;
    }

    /**
     * If the id is null or empty, throw a BadValidationException with the message "Field required". Otherwise, try to get
     * the project by id. If the project is null, throw a BadValidationException with the message "Invalid value".
     * Otherwise, return the id
     * 
     * @param id the value to validate
     * @return The project ID
     */
    public String validateProjectID(String id) throws BadValidationException {
        if(id == null || id.isEmpty()){
            throw new BadValidationException("Field required");
        } else {
            try{
                if(ProjectsRepository.getProjectById(virtualModelLibrary, id) == null){
                    throw new BadValidationException("Invalid value");
                } else {
                    return id;
                }
            } catch (IllegalArgumentException e){
                throw new BadValidationException("Invalid value");
            }
        }
    }

    public String validateFml(String fml) throws BadValidationException {
        if(fml == null || fml.isEmpty()){
            throw new BadValidationException("Field required");
        } else {
            return fml;
        }
    }

    /**
     * It validates the form data and returns a JsonArray of errors
     *
     * @return A JsonArray of JsonObjects. Each JsonObject contains a key-value pair of the form:
     * ```
     * {
     *     "name": "error message"
     * }
     * ```
     */
    public JsonArray validate(){
        String rName        = request.getFormAttribute("name");
        String rDescription = request.getFormAttribute("description");
        String rVisibility  = request.getFormAttribute("visibility");
        String rIsAbstract  = request.getFormAttribute("is_abstract");
        String rProjectId   = request.getFormAttribute("project_id");
        String rPath        = request.getFormAttribute("path");
        String rParentId    = request.getFormAttribute("parent_id");
        errors              = new JsonArray();

        JsonObject errorLine;

        try{
            name = validateName(rName);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("name", e.getMessage());
            errors.add(errorLine);
        }

        try{
            visibility = validateVisibility(rVisibility);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("visibility", e.getMessage());
            errors.add(errorLine);
        }

        try{
            isAbstract = validateBoolean(rIsAbstract);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("is_abstract", e.getMessage());
            errors.add(errorLine);
        }

        try{
            projectId = validateProjectID(rProjectId);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("project_id", e.getMessage());
            errors.add(errorLine);
        }

        try{
            parent = validateParentConcept(rParentId, virtualModelLibrary);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("parent_id", e.getMessage());
            errors.add(errorLine);
        }

        description = validateDescription(rDescription);
        path        = validateDescription(rPath);

        if(errors.isEmpty())
            isValid = true;

        return errors;
    }

    /**
     *  It takes the FML string, validates it, and returns a JsonArray of errors otherwise
     *
     * @return A JsonArray of errors.
     */
    public JsonArray validateFml(){
        JsonObject errorLine;
        errors              = new JsonArray();
        String rFml         = request.getFormAttribute("fml");

        try{
            fml = validateFml(rFml);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("parent_id", e.getMessage());
            errors.add(errorLine);
        }

        if(errors.isEmpty())
            isValid = true;

        return errors;
    }

    /**
     * This method returns true if the object is valid, and false if it is not.
     * 
     * @return The boolean value of isValid.
     */
    public boolean isValid(){
        return isValid;
    }

    /**
     * This method returns the name of the virtual model.
     * 
     * @return The name of the person.
     */
    public String getName() {
        return name;
    }

    /**
     * This method returns the description of the virtual model
     * 
     * @return The description of the item.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the visibility of the virtual model.
     * 
     * @return The visibility of the field.
     */
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * Returns true if this method is abstract, false otherwise.
     * 
     * @return The value of the isAbstract variable.
     */
    public boolean isAbstract() {
        return isAbstract;
    }

    /**
     * This method returns the projectId of the project
     * 
     * @return The projectId
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * This method returns the path of the project
     * 
     * @return The path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the parent FlexoConcept of this FlexoConcept.
     *
     * @return The parent of the FlexoConcept
     */
    public FlexoConcept getParent() {
        return parent;
    }

    /**
     * This function returns the value of the fml variable
     *
     * @return The fml variable is being returned.
     */
    public String getFml() {
        return fml;
    }
}
