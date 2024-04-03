package org.openflexo.http.server.core.validators;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.http.server.core.exceptions.BadValidationException;

/**
 * The Behaviours validator class.
 *
 * @author Ihab Benamer
 */
public class BehavioursValidator extends GenericValidator{
    private final HttpServerRequest request;
    private boolean isValid;
    private JsonArray errors;
    private String name;
    private String description;
    private boolean isAbstract;
    private Class<? extends FlexoBehaviour> type;
    private Visibility visibility;

    /**
     * Instantiates a new Behaviour validator.
     *
     * @param request the request
     */
    public BehavioursValidator(HttpServerRequest request) {
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
        String rName        = request.getFormAttribute("name").trim();
        String rVisibility  = request.getFormAttribute("visibility");
        String rDescription = request.getFormAttribute("description");
        String rIsAbstract  = request.getFormAttribute("is_abstract");
        String rType        = request.getFormAttribute("type");
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
            type = validateBehaviourType(rType);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("type", e.getMessage());
            errors.add(errorLine);
        }

        try{
            isAbstract = validateBoolean(rIsAbstract);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("is_abstract", e.getMessage());
            errors.add(errorLine);
        }

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
     * This function returns the name of the person.
     *
     * @return The name of the person.
     */
    public String getName() {
        return name;
    }

    /**
     * This function returns the description of the object
     *
     * @return The description of the item.
     */
    public String getDescription() {
        return description;
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
     * Returns the type of the behaviour.
     *
     * @return The type of the FlexoBehaviour.
     */
    public Class<? extends FlexoBehaviour> getType() {
        return type;
    }

    /**
     * Returns the visibility of the field.
     *
     * @return The visibility of the field.
     */
    public Visibility getVisibility() {
        return visibility;
    }
}
