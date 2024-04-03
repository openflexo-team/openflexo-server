package org.openflexo.http.server.core.controllers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.cli.CommandInterpreter;
import org.openflexo.foundation.fml.cli.ParseException;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.http.server.core.RestTerminal;
import org.openflexo.http.server.core.repositories.TerminalRepository;
import org.openflexo.http.server.core.serializers.JsonSerializer;
import org.openflexo.http.server.core.validators.TerminalValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 *  Terminal rest apis controller.
 * @author Ihab Benamer
 */
public class TerminalController extends GenericController {
    private final VirtualModelLibrary virtualModelLibrary;
    private final static String ROOT_WORKSPACE = "workspace/";

    /**
     * Instantiates a new Terminal controller.
     *
     * @param virtualModelLibrary virtual model library
     */
    public TerminalController(VirtualModelLibrary virtualModelLibrary) {
        this.virtualModelLibrary    = virtualModelLibrary;
    }

    /**
     * It creates a new terminal, registers it in the terminal repository, and returns the terminal's session ID and prompt
     *
     * @param context the routing context
     */
    public void init(RoutingContext context) {
        FlexoServiceManager serviceManager  = virtualModelLibrary.getServiceManager();
        try {
            RestTerminal terminal = new RestTerminal(serviceManager, Files.createDirectories(Paths.get(ROOT_WORKSPACE)).toFile().getAbsolutePath());
            TerminalRepository.registerTerminal(terminal);

            JsonObject result = new JsonObject();

            result.put("session_id", terminal.getSessionID());
            result.put("prompt", terminal.getCi().getPrompt());

            context.response().end(result.encodePrettily());

        } catch (IOException e) {
            badRequest(context);
        }
    }

    /**
     * If the session ID is not null, then get the terminal from the repository and return the session ID and prompt
     *
     * @param context The routing context.
     */
    public void get(RoutingContext context) {
        String sessionId = context.request().getFormAttribute("session_id");

        if (sessionId != null){
            RestTerminal terminal = TerminalRepository.getTerminal(sessionId);

            if (terminal != null) {
                JsonObject result = new JsonObject();

                result.put("session_id", terminal.getSessionID());
                result.put("prompt", terminal.getCi().getPrompt());

                context.response().end(result.encodePrettily());
            } else {
                notFound(context);
            }
        } else {
            notFound(context);
        }
    }

    /**
     * If the terminal is valid, execute the command and return the output
     *
     * @param context The routing context.
     */
    public void execute(RoutingContext context) {
        TerminalValidator validator = new TerminalValidator(context.request());
        JsonArray errors            = validator.validate();

        if(validator.isValid()){
            try {
                RestTerminal terminal = TerminalRepository.getTerminal(validator.getSessionId());

                if(terminal == null || terminal.getCi() == null){
                    notFound(context);
                } else {
                    CommandInterpreter ci   = terminal.getCi();
                    String command          = validator.getCommand().trim();

                    if(command.toLowerCase().trim().startsWith("cd")){

                        ci.executeCommand("pwd");

                        String root         = new File(ROOT_WORKSPACE).getAbsolutePath();
                        String currentDir   = ci.getOutput().get(0);
                        String path         = command.replace("cd", "").trim();

                        if (path.startsWith("/")){
                            path = root + path;
                        }

                        ci.executeCommand("cd " + path);
                        ci.executeCommand("pwd");

                        if(ci.getOutput().get(0).contains(root)){
                            context.response().end(JsonSerializer.terminalOutputSerializer(Collections.emptyList(), terminal).encodePrettily());
                        } else {
                            ci.executeCommand("cd " + currentDir);
                            notAuthorized(context);
                        }
                    } else {
                        ci.executeCommand(command);

                        List<String> output = ci.getOutput();

                        if(command.equalsIgnoreCase("pwd")){
                            output.set(0, ci.getOutput().get(0).replace(new File(ROOT_WORKSPACE).getAbsolutePath(), "/").replace("//", "/"));
                        }

                        context.response().end(JsonSerializer.terminalOutputSerializer(output, terminal).encodePrettily());
                    }
                }
            } catch (FMLCommandExecutionException | ParseException e) {
                JsonObject error = new JsonObject();

                error.put("command", e.getMessage());
                errors.add(error);

                badValidation(context, errors);
            }
        } else {
            badValidation(context, errors);
        }
    }

    /**
     * If the terminal exists, return the history of the terminal
     *
     * @param context The routing context is the object that contains all the information about the request and response.
     */
    public void history(RoutingContext context) {
        String  sessionId       = context.request().getFormAttribute("session_id");
        RestTerminal terminal   = TerminalRepository.getTerminal(sessionId);
        
        if(terminal == null){
            notFound(context);

        } else {
            context.response().end(JsonSerializer.terminalHistorySerializer(terminal.getCi().getHistory()).encodePrettily());
        }
    }

    /**
     * This function validates a terminal command and returns a list of available completions for that command.
     *
     * @param context The context object represents the current HTTP request/response context. It contains information
     * about the incoming request (such as headers, parameters, and body) and provides methods to send a response back to
     * the client.
     */
    public void completion(RoutingContext context) {
        TerminalValidator validator = new TerminalValidator(context.request());
        JsonArray errors            = validator.validate();

        if(validator.isValid()){
            RestTerminal terminal = TerminalRepository.getTerminal(validator.getSessionId());

            if(terminal == null || terminal.getCi() == null){
                notFound(context);
            } else {
                List<String> availableCompletion = terminal.getCi().getAvailableCompletion(validator.getCommand());

                context.response().end(JsonSerializer.terminalCompletionSerializer(availableCompletion).encodePrettily());
            }
        } else {
            badValidation(context, errors);
        }
    }

    public void kill(RoutingContext context) {
        String  sessionId       = context.request().getFormAttribute("session_id");
        RestTerminal terminal   = TerminalRepository.getTerminal(sessionId);

        if(terminal == null){
            notFound(context);
        } else {
            TerminalRepository.removeTerminal(sessionId);
            emptyResponse(context);
        }
    }
}
