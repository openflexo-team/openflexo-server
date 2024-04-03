package org.openflexo.http.server.core.repositories;

import org.openflexo.http.server.core.RestTerminal;

import java.util.HashMap;

public class TerminalRepository {
    private static HashMap<String, RestTerminal> terminals = new HashMap();

    public static HashMap<String, RestTerminal> getAllTerminals() {
        return terminals;
    }

    public static void registerTerminal(RestTerminal terminal) {
        terminals.put(terminal.getSessionID(), terminal);
    }

    public static RestTerminal getTerminal(String key) {
        return terminals.get(key);
    }

    public static void removeTerminal(String key) {
        terminals.remove(key);
    }
}
