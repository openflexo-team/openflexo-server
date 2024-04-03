package org.openflexo.http.server.websocket.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import org.openflexo.http.server.connie.ConnieMessage;

public class GraphEditorXmlChangeRequest  extends ConnieMessage {
    public final String id;
    public final String content;

    public GraphEditorXmlChangeRequest(
            @JsonProperty("id") String id,
            @JsonProperty("content") String content
    ) {
        this.id         = id;
        this.content    = content;
    }

    @Override
    public String toString() {
        return "Xml change request : " + id;
    }

    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.put("id", this.id);
        result.put("content", this.content);

        return result;
    }
}
