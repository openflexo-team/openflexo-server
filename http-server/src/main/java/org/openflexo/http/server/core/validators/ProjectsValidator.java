package org.openflexo.http.server.core.validators;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.http.server.core.exceptions.BadValidationException;
import org.openflexo.http.server.core.repositories.ResourceCentersRepository;

import java.util.Iterator;
import java.util.List;

/**
 * The Projects validator class.
 *
 * @author Ihab Benamer
 */
public class ProjectsValidator extends GenericValidator{
    private final ProjectLoader projectLoader;
    private final HttpServerRequest request;
    private boolean isValid;
    private JsonArray errors;
    private String name;
    private String rcId;

    /**
     * Instantiates a new Projects validator.
     *
     * @param projectLoader the project loader
     * @param request       the request
     */
    public ProjectsValidator(ProjectLoader projectLoader, HttpServerRequest request) {
        this.projectLoader  = projectLoader;
        this.request        = request;
        isValid             = false;
    }

    /**
     * If the id is null or empty, throw a BadValidationException. If the id is not null or empty, check if the 
     *  resource center exists. If it does, return the id. If it doesn't, throw a BadValidationException
     * 
     * @param id The id of the resource center to validate
     * @return The id of the resource center
     */
    public String validateResourceCenterID(String id) throws BadValidationException {
        if(id == null || id.isEmpty()){
            throw new BadValidationException("Invalid value");
        } else {
            if(ResourceCentersRepository.getResourceCenterById(projectLoader, id) == null){
                throw new BadValidationException("Field required");
            } else {
                return id;
            }
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
        String rName    = request.getFormAttribute("name");
        String rRcId    = request.getFormAttribute("resource_center_id");
        errors          = new JsonArray();

        JsonObject errorLine;

        try{
            name = validateString(rName);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("name", e.getMessage());
            errors.add(errorLine);
        }

        try{
            rcId = validateResourceCenterID(rRcId);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("resource_center_id", e.getMessage());
            errors.add(errorLine);
        }

        if(errors.isEmpty())
            isValid = true;

        return errors;
    }

    public JsonArray validateUpload(List<FileUpload> fileUploads) {
        String rRcId    = request.getFormAttribute("resource_center_id");
        Iterator<FileUpload> fileUploadIterator = fileUploads.iterator();
        errors                                  = new JsonArray();

        JsonObject errorLine;

        if(fileUploads.isEmpty()){
            errorLine = new JsonObject();
            errorLine.put("uploads", "No file uploaded");
            errors.add(errorLine);
        } else {
            int counter =1;

            while (fileUploadIterator.hasNext()){
                try{
                    validateFileUpload(fileUploadIterator);
                } catch (BadValidationException e){
                    errorLine = new JsonObject();
                    errorLine.put("File "+ counter, e.getMessage());
                    errors.add(errorLine);
                }

                counter++;
            }
        }

        try{
            rcId = validateResourceCenterID(rRcId);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("resource_center_id", e.getMessage());
            errors.add(errorLine);
        }

        if(errors.isEmpty())
            isValid = true;

        return errors;
    }

    /**
     * Returns true if the object is valid, false otherwise.
     * 
     * @return The boolean value of isValid.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * This method returns the name of the project.
     * 
     * @return The name of the person.
     */
    public String getName() {
        return name;
    }

    /**
     * > This method returns the rcId
     * 
     * @return The rcId is being returned.
     */
    public String getRcId() {
        return rcId;
    }
}
