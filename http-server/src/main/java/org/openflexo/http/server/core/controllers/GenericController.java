package org.openflexo.http.server.core.controllers;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.rt.error.FMLErrorReporter;
import org.openflexo.http.server.core.serializers.JsonSerializer;

/**
 * A Generic controller for managing exceptional http status
 *
 * @author Ihab Benamer
 */
public abstract class GenericController {

    private static final Logger logger = Logger.getLogger(GenericController.class.getPackage().getName());

    /**
     * Answer supplied failure of FML: its JSON ({@link JsonSerializer#fmlErrorSerializer(Throwable)}), with the status 400 for an error
     * foreseen by FML (the FML or its data are at fault) and 500 for a defect of the platform (CORE-F-11). A defect not reported yet is
     * logged with its Java stack.
     *
     * @param context The routing context.
     * @param failure The failure, as thrown or as given by {@link FlexoAction#getFailure()}.
     */
    public void fmlError(RoutingContext context, Throwable failure) {
        boolean foreseen = FMLErrorReporter.isForeseen(failure);
        if (!foreseen && !FMLErrorReporter.isReported(failure)) {
            logger.log(Level.SEVERE, "Unexpected failure", failure);
        }
        HttpServerResponse response = context.response();
        response.putHeader("content-type", "application/json");
        response.setStatusCode(foreseen ? 400 : 500);
        response.end(JsonSerializer.fmlErrorSerializer(failure).encodePrettily());
    }

    /**
     * Execute supplied action; when it does not succeed, answer its failure ({@link #fmlError(RoutingContext, Throwable)}), or 409 when
     * it was not executed because it is not enabled, and return false: the caller then stops, and saves nothing (CORE-F-11). The edits a
     * failing action made before its failure stay in memory (SERVER-D-1)
     *
     * @param context The routing context.
     * @param action The action to execute.
     * @return true when the action succeeded
     */
    public boolean performAction(RoutingContext context, FlexoAction<?, ?, ?> action) {
        try {
            action.doAction();
        } catch (RuntimeException e) {
            // A defect of the platform, reported by the editor and rethrown
            fmlError(context, e);
            return false;
        }
        if (action.hasActionExecutionSucceeded()) {
            return true;
        }
        Throwable failure = action.getFailure();
        if (failure != null) {
            fmlError(context, failure);
        }
        else {
            // Not executed, without a failure: the action is not enabled (a false @Enable of a behaviour)
            HttpServerResponse response = context.response();
            response.putHeader("content-type", "application/json");
            response.setStatusCode(409);
            response.end(JsonSerializer.fmlNotEnabledSerializer(
                    "Action " + action.getLocalizedName() + " was not executed: it is not enabled").encodePrettily());
        }
        return false;
    }

    /**
     * If the request is not found, then set the status code to 404 and close the connection
     *
     * @param context The routing context.
     */
    public void notFound(RoutingContext context) {
        context.response().setStatusCode(404).end();
    }

    /**
     * It takes a RoutingContext and an Exception, and writes a JSON response to the client with the exception's class
     * name, message, and stack trace
     *
     * @param context The routing context of the request.
     * @param e The exception that was thrown
     */

    public void error(RoutingContext context, Exception e) {
        HttpServerResponse response = context.response();
        response.write("{ error: '"+ e.getClass().getSimpleName() +"'; message: '" + e.getMessage() +"'; stackTrace: ''}");
        response.setStatusCode(500);
        response.close();
    }

    /**
     * If the request is bad, send a 400 response.
     *
     * @param context The routing context is the object that contains all the information about the request and the
     * response.
     */
    public void badRequest(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.setStatusCode(400);
        response.end();
    }

    /**
     * If the request is bad, send a 400 response with the given message.
     *
     * @param context The routing context.
     * @param message The message to send to the client.
     */
    public void badRequest(RoutingContext context, String message) {
        HttpServerResponse response = context.response();
        response.setStatusCode(400);
        response.end(message);
    }

    /**
     * It takes a RoutingContext and a JsonArray, sets the status code to 422, and then sends the JsonArray as the response
     *
     * @param context The routing context.
     * @param errors The array of errors returned by the validation.
     */
    public void badValidation(RoutingContext context, JsonArray errors) {
        HttpServerResponse response = context.response();
        response.setStatusCode(422);
        response.end(errors.encodePrettily());
    }

    /**
     * Set the status code to 204 and end the response.
     *
     * @param context The routing context is the object that contains the request and response objects.
     */
    public void emptyResponse(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.setStatusCode(204);
        response.end();
    }

    public void notAuthorized(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.setStatusCode(403);
        response.end();
    }
}
