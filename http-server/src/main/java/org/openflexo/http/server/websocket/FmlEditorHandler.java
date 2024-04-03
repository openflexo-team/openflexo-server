package org.openflexo.http.server.websocket;

import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.http.server.connie.*;
import org.openflexo.http.server.core.TechnologyAdapterRouteService;
import org.openflexo.http.server.websocket.requests.FmlEditorCursorChangeRequest;
import org.openflexo.http.server.websocket.requests.FmlEditorRequest;
import org.openflexo.http.server.websocket.requests.GraphEditorXmlChangeRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * WebSocket service to evaluate Connie expressions
 */
public class FmlEditorHandler  implements Handler<ServerWebSocket> {

    private final FlexoServiceManager serviceManager;
    private final TechnologyAdapterRouteService taRouteService;
    private final Set<WebSocketClient> clients = new HashSet<>();
    private String sourceCode;
    private String xmlCanvas;

    public FmlEditorHandler(FlexoServiceManager serviceManager, TechnologyAdapterRouteService taRouteService) {
        this.serviceManager = serviceManager;
        this.taRouteService = taRouteService;
    }

    private void register(WebSocketClient client) {
        clients.add(client);
    }

    private void unregister(WebSocketClient client) {
        clients.remove(client);
    }

    @Override
    public void handle(ServerWebSocket socket) {
        register(new WebSocketClient(socket));
    }

    private class WebSocketClient {
        private final ServerWebSocket socket;
        private FmlEditorCursorChangeRequest cursor;
        private String type;

        public FmlEditorCursorChangeRequest getCursor() {
            return cursor;
        }

        public WebSocketClient(ServerWebSocket socket) {
            this.socket = socket;

            socket.frameHandler(this::handleFrame);
            socket.exceptionHandler(this::exceptionHandler);
            socket.endHandler(this::endHandler);

            if (this.socket.path().startsWith("/FmlEditor")) {
                type = "FmlEditor";

                this.respondToFmlEditionRequest(null);
            } else if (this.socket.path().startsWith("/GraphEditor")) {
                type = "GraphEditor";
                
                this.respondToGraphXmlChangeRequest(null);
            } else {
                type = "other";
            }
        }

        private void handleFrame(WebSocketFrame frame) {
            try {
                ConnieMessage message = ConnieMessage.read(frame.textData());

                if (message instanceof FmlEditorRequest) {
                    sourceCode = ((FmlEditorRequest) message).code;
                    for (WebSocketClient client: clients){
                        if (!client.equals(this) && client.type.equals("FmlEditor")){
                            client.respondToFmlEditionRequest((FmlEditorRequest) message);
                        }
                    }

                    List<FmlEditorCursorChangeRequest> cursors = clients.stream().map(WebSocketClient::getCursor).collect(Collectors.toList());

                    for (WebSocketClient client: clients){
                        if (client.type.equals("FmlEditor")){
                            client.respondToCursorChangeRequest(cursors);
                        }
                    }
                }  else if (message instanceof FmlEditorCursorChangeRequest){
                    cursor = (FmlEditorCursorChangeRequest) message;

                    if(cursor != null && cursor.init && !sourceCode.isEmpty()) {
                        JsonObject response = new JsonObject();

                        response.put("type", "code");
                        response.put("content", sourceCode);

                        socket.writeTextMessage(response.encodePrettily());
                    }

                    List<FmlEditorCursorChangeRequest> cursors = clients.stream().map(WebSocketClient::getCursor).collect(Collectors.toList());

                    for (WebSocketClient client: clients){
                        if (client.type.equals("FmlEditor")) {
                            client.respondToCursorChangeRequest(cursors);
                        }
                    }
                }  else if (message instanceof GraphEditorXmlChangeRequest){
                    xmlCanvas = ((GraphEditorXmlChangeRequest) message).content;
                    for (WebSocketClient client: clients){
                        if (!client.equals(this) && client.type.equals("GraphEditor")){
                            client.respondToGraphXmlChangeRequest((GraphEditorXmlChangeRequest) message);
                        }
                    }
                }
            } catch (DecodeException e) {
                String message = "Can't decode request: " + e.getMessage();
                System.out.println(message);
                socket.write(Response.error(message).toBuffer());
            }
        }
        
        private void respondToFmlEditionRequest(FmlEditorRequest request) {
            if(request != null && !request.code.isEmpty()){
                sourceCode = request.code;
            }

            JsonObject response = new JsonObject();
            response.put("type", "code");
            response.put("content", sourceCode);

            // TODO : handle fml code here

            socket.writeTextMessage(response.encodePrettily());
        }
        
        private void respondToCursorChangeRequest(List<FmlEditorCursorChangeRequest> request) {
            JsonArray message = new JsonArray();

            for (FmlEditorCursorChangeRequest cursor: request) {
                message.add(cursor);
            }

            JsonObject response = new JsonObject();
            response.put("type", "cursor");
            response.put("content", message);

            socket.writeTextMessage(response.encodePrettily());
        }
        
        private void respondToGraphXmlChangeRequest(GraphEditorXmlChangeRequest message) {
            if(message != null && !message.content.isEmpty()){
                xmlCanvas = message.content;
            }
            
            JsonObject response = new JsonObject();

            response.put("type", "xml");
            response.put("content", xmlCanvas);

            socket.writeTextMessage(response.encodePrettily());
        }

        private void exceptionHandler(Throwable throwable) {
            System.err.println("Exception in websocket " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
        }

        private void endHandler(Void nothing) {
            unregister(this);
        }
    }
}
