package org.openflexo.http.server.core.validators;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.http.server.core.exceptions.BadValidationException;
import org.openflexo.http.server.util.IdUtils;

import java.io.FileNotFoundException;

/**
 * The Virtual Model instances validator class.
 *
 * @author Ihab Benamer
 */
public class VirtualModelInstancesValidator extends GenericValidator{
    private final VirtualModelLibrary virtualModelLibrary;
    private final HttpServerRequest request;
    private boolean isValid;
    private JsonArray errors;
    private String name;
    private String title;

    /**
     * Instantiates a new Virtual model instance validator.
     *
     * @param request             the request
     * @param virtualModelLibrary the virtual model library
     */
    public VirtualModelInstancesValidator(HttpServerRequest request, VirtualModelLibrary virtualModelLibrary) {
        this.request                = request;
        this.virtualModelLibrary    = virtualModelLibrary;
        isValid                     = false;
    }

    /**
     * It checks if the id is valid and if it corresponds to a virtual model in the project
     * 
     * @param id the id of the virtual model to validate
     * @return The id of the virtual model
     */
    public String validateVirtualModelId(String id) throws BadValidationException {
        if(id == null || id.isEmpty()){
            throw new BadValidationException("Field required");
        } else {
            try{
                if(virtualModelLibrary.getVirtualModel(IdUtils.decodeId(id)) == null){
                    throw new BadValidationException("Invalid value");
                } else {
                    return id;
                }
            } catch (ResourceLoadingCancelledException | FlexoException | FileNotFoundException e){
                throw new BadValidationException("Invalid value");
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
        String rTitle   = request.getFormAttribute("title");
        errors          = new JsonArray();

        JsonObject errorLine;

        try{
            name = validateName(rName);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("name", e.getMessage());
            errors.add(errorLine);
        }

        try{
            title = validateName(rTitle);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("title", e.getMessage());
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
     * This method returns the name of the virtual model instance.
     * 
     * @return The name of the person.
     */
    public String getName() {
        return name;
    }

    /**
     * This method returns the title of the virtual model instance
     * 
     * @return The title of the book.
     */
    public String getTitle() {
        return title;
    }

}
