package org.openflexo.http.server.core.controllers;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

/**
 * A Generic controller for managing exceptional http status
 *
 * @author Ihab Benamer
 */
public abstract class GenericController {

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
