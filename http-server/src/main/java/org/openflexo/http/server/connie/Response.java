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
import java.util.Objects;

/**
 * Response object for response
 */
public final class Response extends ConnieMessage {

	public long id;

	public Object result;

	public String error;

	public Response(long id) {
		this(id, null, null);
	}

	public Response(
			@JsonProperty("id") long id,
			@JsonProperty("result") Object result,
			@JsonProperty("error") String error
	) {
		this.id = id;
		this.result = result;
		this.error = error;
	}
	@Override
	public String toString() {
		return "Response " + error != null ? "[error]: " + error : Objects.toString(result);
	}

	public static Response error(String error) {
		return error(-1, error);
	}

	public static Response error(long id, String error) {
		return new Response(id, null, error);
	}
}
