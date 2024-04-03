package org.openflexo.http.server.core.validators;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import org.openflexo.http.server.core.exceptions.BadValidationException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The Resource centers validator class.
 *
 * @author Ihab Benamer
 */
public class ResourceCentersValidator extends GenericValidator {

    private final HttpServerRequest request;
    private boolean isValid;
    private JsonArray errors;
    private String rcPath;

    /**
     * Instantiates a new Resource centers validator.
     *
     * @param request the request
     */
    public ResourceCentersValidator(HttpServerRequest request) {
        this.request    = request;
        isValid         = false;
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
        String rRcPath  = request.getFormAttribute("rc_path");
        errors          = new JsonArray();

        JsonObject errorLine;

        try{
            rcPath = validatePath(rRcPath);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("rc_path", e.getMessage());
            errors.add(errorLine);
        }

        if(errors.isEmpty())
            isValid = true;

        return errors;
    }

    /**
     * > It takes a set of file uploads, iterates through them, and validates each one
     * 
     * @param fileUploadSet A set of file uploads
     * @return A JsonArray of errors.
     */
    public JsonArray validateUpload(List<FileUpload> fileUploadSet){
        Iterator<FileUpload> fileUploadIterator = fileUploadSet.iterator();
        errors                                  = new JsonArray();

        JsonObject errorLine;

        if(fileUploadSet.isEmpty()){
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
     * This method returns the path to the resource file
     * 
     * @return The path to the resource file.
     */
    public String getRcPath() {
        return rcPath;
    }
}
