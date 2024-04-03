package org.openflexo.http.server.core;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.cli.CommandInterpreter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class RestTerminal {
    private String sessionID;
    private CommandInterpreter ci;

    public RestTerminal(FlexoServiceManager serviceManager, String path) throws IOException {
        UUID uuid   = UUID.randomUUID();
        sessionID   = uuid.toString();
        ci          = new CommandInterpreter(serviceManager, new RestInputStream(), new RestOutputStream(), new RestOutputStream(), new File(path));
    }

    public RestTerminal(FlexoServiceManager serviceManager, String path, String sessionId) throws IOException {
        sessionID   = sessionId;
        ci          = new CommandInterpreter(serviceManager, new RestInputStream(), new RestOutputStream(), new RestOutputStream(), new File(path));
    }

    public class RestOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {

        }
    }

    public class RestInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            return 0;
        }
    }

    public CommandInterpreter getCi() {
        return ci;
    }

    public String getSessionID() {
        return sessionID;
    }
}
