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
import java.util.HashMap;
import java.util.Map;

/**
 * Id class for a {@link org.openflexo.connie.DataBinding} with a given runtime context
 */
public class RuntimeBindingId {

	public final BindingId binding;

	public final String runtimeUrl;

	public final Map<String, String> extensions = new HashMap<>();

	public RuntimeBindingId(
			@JsonProperty("binding") BindingId binding,
			@JsonProperty("runtimeUrl") String runtimeUrl,
			@JsonProperty("extensions") Map<String, String> extensions
	) {
		this.binding = binding;
		this.runtimeUrl = runtimeUrl;
		if (extensions != null) {
			this.extensions.putAll(extensions);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		RuntimeBindingId that = (RuntimeBindingId) o;

		if (binding != null ? !binding.equals(that.binding) : that.binding != null)
			return false;
		if (!runtimeUrl.equals(that.runtimeUrl))
			return false;
		return extensions != null ? extensions.equals(that.extensions) : that.extensions == null;
	}

	@Override
	public int hashCode() {
		int result = binding != null ? binding.hashCode() : 0;
		result = 31 * result + runtimeUrl.hashCode();
		result = 31 * result + (extensions != null ? extensions.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "RuntimeBindingId{" + "binding=" + binding + ", runtimeUrl='" + runtimeUrl + '\'' + '}';
	}
}
