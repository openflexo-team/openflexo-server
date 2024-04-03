package org.openflexo.http.server.core.validators;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.http.server.core.exceptions.BadValidationException;

public class EnumsValidator extends GenericValidator{
    private final HttpServerRequest request;
    private boolean isValid;
    private JsonArray errors;
    private String  name;
    private String description;
    private Visibility visibility;
    private boolean isAbstract;

    public EnumsValidator(HttpServerRequest request) {
        this.request    = request;
        isValid         = false;
    }

    public JsonArray validate(){
        String rName        = request.getFormAttribute("name").trim();
        String rDescription = request.getFormAttribute("description");
        String rVisibility  = request.getFormAttribute("visibility");
        String rIsAbstract  = request.getFormAttribute("is_abstract");
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

        description = validateDescription(rDescription);

        if(errors.isEmpty())
            isValid = true;

        return errors;
    }

    public JsonArray validateValue(){
        String rName        = request.getFormAttribute("name").trim();
        String rDescription = request.getFormAttribute("description");
        errors              = new JsonArray();

        JsonObject errorLine;

        try{
            name = validateString(rName);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("name", e.getMessage());
            errors.add(errorLine);
        }

        description = validateDescription(rDescription);

        if(errors.isEmpty())
            isValid = true;

        return errors;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public boolean isAbstract() {
        return isAbstract;
    }
}
