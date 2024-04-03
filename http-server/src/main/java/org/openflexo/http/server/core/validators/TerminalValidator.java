package org.openflexo.http.server.core.validators;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.openflexo.http.server.core.exceptions.BadValidationException;

public class TerminalValidator extends GenericValidator {
    private final HttpServerRequest request;
    private boolean isValid;
    private JsonArray errors;
    private String sessionId;
    private String command;

    public TerminalValidator(HttpServerRequest request) {
        this.request    = request;
        isValid         = false;
    }

    public JsonArray validate(){
        String rCommand     = request.getFormAttribute("command");
        String rSessionId   = request.getFormAttribute("session_id");
        errors              = new JsonArray();

        JsonObject errorLine;

        try{
            command = validateString(rCommand);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("command", e.getMessage());
            errors.add(errorLine);
        }

        sessionId = validateDescription(rSessionId);

        if(errors.isEmpty())
            isValid = true;

        return errors;
    }

    public boolean isValid() {
        return isValid;
    }

    public JsonArray getErrors() {
        return errors;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getCommand() {
        return command;
    }
}
