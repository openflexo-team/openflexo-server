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

package org.openflexo.http.gina;

import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.http.server.json.JsonComplement;
import org.openflexo.http.server.json.JsonSerializer;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.technologyadapter.gina.model.GINAFIBComponent;

import io.vertx.core.json.JsonObject;

public class GinaJsonComplement implements JsonComplement {

	@Override
	public PamelaModelFactory getFactory() throws ModelDefinitionException {
		return new FIBModelFactory(null);
	}

	private static void describeComponent(JsonSerializer serializer, FIBComponent component, JsonObject result, boolean detailed) {
		result.put("name", component.getName());
		result.put("variables", serializer.toArray(component.getVariables(), detailed));

		if (component instanceof FIBContainer) {
			FIBContainer container = (FIBContainer) component;
			result.put("subComponents", serializer.toArray(container.getSubComponents(), detailed));
			result.put("layout", serializer.toJson(container.getLayout(), detailed));
		}
	}

	@Override
	public void describeObject(JsonSerializer serializer, Object object, JsonObject result, boolean detailed) {
		if (object instanceof GINAFIBComponent) {
			GINAFIBComponent component = (GINAFIBComponent) object;
			result.put("component", serializer.toJson(component.getComponent(), detailed));
		}

		if (object instanceof FIBComponent) {
			serializer.describeObject(object, result, true);
		}
	}
}
