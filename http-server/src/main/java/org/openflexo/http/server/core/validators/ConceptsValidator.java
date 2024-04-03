package org.openflexo.http.server.core.validators;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.http.server.core.exceptions.BadValidationException;
import java.util.List;

/**
 * The Concepts validator class.
 *
 * @author Ihab Benamer
 */
public class ConceptsValidator extends GenericValidator{
    private final VirtualModelLibrary virtualModelLibrary;
    private final HttpServerRequest request;
    private boolean isValid;
    private JsonArray errors;
    private String name;
    private Visibility visibility;
    private boolean isAbstract;
    private String description;
    private FlexoConcept parent;

    /**
     * Instantiates a new Concept validator.
     *
     * @param request the request
     */
    public ConceptsValidator(HttpServerRequest request, VirtualModelLibrary virtualModelLibrary) {
        this.virtualModelLibrary    = virtualModelLibrary;
        this.request                = request;
        isValid                     = false;
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
        String rName            = request.getFormAttribute("name").trim();
        String rVisibility      = request.getFormAttribute("visibility");
        String rDescription     = request.getFormAttribute("description");
        String rIsAbstract      = request.getFormAttribute("is_abstract");
        String rParentId        = request.getFormAttribute("parent_id");
        errors                  = new JsonArray();

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
            parent = validateParentConcept(rParentId, virtualModelLibrary);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("parent_id", e.getMessage());
            errors.add(errorLine);
        }

        // TODO: support multiple parent concepts

        description = validateDescription(rDescription);

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
     * This method returns the name of the person.
     * 
     * @return The name of the person.
     */
    public String getName() {
        return name;
    }

    /**
     * > This method returns the visibility of the current object
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
     * > This method returns the description of the object
     * 
     * @return The description of the item.
     */
    public String getDescription() {
        return description;
    }

    public FlexoConcept getParent() {
        return parent;
    }
}
