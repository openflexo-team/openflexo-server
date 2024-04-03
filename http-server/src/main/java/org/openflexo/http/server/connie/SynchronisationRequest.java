/*
 * Copyright (c) 2013-2017, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
 * developed at Openflexo.
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or any later version ), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 *
 * You can redistribute it and/or modify under the terms of either of these licenses
 *
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *           Additional permission under GNU GPL version 3 section 7
 *           If you modify this Program, or any covered work, by linking or
 *           combining it with software containing parts covered by the terms
 *           of EPL 1.0, the licensors of this Program grant you additional permission
 *           to convey the resulting work.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.openflexo.org/license.html for details.
 *
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.http.server.connie;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Simple class representing a custom synchronizing request from a client through a WebSocket
 */
public class SynchronisationRequest extends ConnieMessage {

    public static class Range {
        public int startLineNumber;
        public int startColumn;
        public int endLineNumber;
        public int endColumn;

        public Range (
                @JsonProperty("endColumn") int endColumn,
                @JsonProperty("endLineNumber") int endLineNumber,
                @JsonProperty("startColumn") int startColumn,
                @JsonProperty("startLineNumber") int startLineNumber
        ) {
            this.endColumn          = endColumn;
            this.endLineNumber      = endLineNumber;
            this.startColumn        = startColumn;
            this.startLineNumber    = startLineNumber;
        }
    }

    public static class Change  {
        public boolean forceMoveMarkers;
        public Range range;
        public int rangeLength;
        public int rangeOffset;
        public String text;

        public Change (
                @JsonProperty("forceMoveMarkers") boolean forceMoveMarkers,
                @JsonProperty("range") Range range,
                @JsonProperty("rangeLength") int rangeLength,
                @JsonProperty("rangeOffset") int rangeOffset,
                @JsonProperty("text") String text
        ) {
            this.forceMoveMarkers   = forceMoveMarkers;
            this.range   			= range;
            this.rangeLength        = rangeLength;
            this.rangeOffset        = rangeOffset;
            this.text               = text;
        }
    }

    public final long id;
    public final List<Change> changes;

    public SynchronisationRequest(
            @JsonProperty("id") long id,
            @JsonProperty("changes") List<Change> changes
    ) {
        this.id             = id;
        this.changes        = changes;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
