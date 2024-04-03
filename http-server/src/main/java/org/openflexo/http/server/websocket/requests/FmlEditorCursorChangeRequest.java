package org.openflexo.http.server.websocket.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import org.openflexo.http.server.connie.ConnieMessage;

public class FmlEditorCursorChangeRequest extends ConnieMessage {
    public static class Position {
        public final int lineNumber;
        public final int column;

        public Position(
                @JsonProperty("lineNumber") int lineNumber,
                @JsonProperty("column") int column
        ){
            this.lineNumber = lineNumber;
            this.column     = column;
        }
    }

    public final String id;
    public final String label;
    public final String color;
    public final Position position;
    public final boolean init;

    public FmlEditorCursorChangeRequest(
            @JsonProperty("id") String id,
            @JsonProperty("label") String label,
            @JsonProperty("color") String color,
            @JsonProperty("position") Position position,
            @JsonProperty("init") boolean init
    ){
        this.id         = id;
        this.label      = label;
        this.color      = color;
        this.position   = position;
        this.init       = init;
    }

    @Override
    public String toString() {
        return "Cursor : " + id;
    }

    public JsonObject toJson() {
        JsonObject position = new JsonObject();
        position.put("lineNumber", this.position.lineNumber);
        position.put("column", this.position.column);

        JsonObject cursor = new JsonObject();
        cursor.put("id", this.id);
        cursor.put("label", this.label);
        cursor.put("color", this.color);
        cursor.put("position", position);

        return cursor;
    }
}