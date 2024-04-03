package org.openflexo.http.server.websocket.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openflexo.http.server.connie.ConnieMessage;


public class FmlEditorRequest extends ConnieMessage {
    public final long id;
    public final String code;

    public FmlEditorRequest(
            @JsonProperty("id") long id,
            @JsonProperty("code") String code
    ) {
        this.id         = id;
        this.code       = code;
    }

    @Override
    public String toString() {
        return "request : " + id;
    }
}
