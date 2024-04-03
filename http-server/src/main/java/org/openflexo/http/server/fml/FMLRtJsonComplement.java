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

package org.openflexo.http.server.fml;

import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.http.server.json.JsonComplement;
import org.openflexo.http.server.json.JsonSerializer;

import io.vertx.core.json.JsonObject;

/**
 * Json Complement for FML@runtime
 */
public class FMLRtJsonComplement implements JsonComplement {

	/*
	@Override
	public boolean identifyObject(Object object, JsonObject result) {
		if (object instanceof FMLRTVirtualModelInstance) {
			result.put("name", ((FMLRTVirtualModelInstance) object).getName());
		}
		return super.identifyObject(object, result);
	}
	*/

	private static void describeFlexoConceptInstance(JsonSerializer serializer, FlexoConceptInstance instance, JsonObject result,
			boolean detailed) {

		FlexoConcept flexoConcept = instance.getFlexoConcept();
		/*if (flexoConcept instanceof ViewPoint) {
			result.put("viewPoint", serializer.toReference(flexoConcept));
		} else*/ if (flexoConcept instanceof VirtualModel) {
			// result.put("view", serializer.toReference(instance.getView()));
			result.put("virtualModel", serializer.toReference(flexoConcept));
		}
		else {
			result.put("flexoConcept", serializer.toReference(flexoConcept));

			result.put("virtualModelInstance", serializer.toReference(instance.getVirtualModelInstance()));
			// result.put("view", serializer.toReference(instance.getView()));
			result.put("container", serializer.toReference(instance.getContainerFlexoConceptInstance()));
		}

		result.put("actors", serializer.toMap(instance.getActors(), ActorReference::getRoleName, detailed));
		result.put("embeddedFlexoConceptInstance", serializer.toReferenceArray(instance.getEmbeddedFlexoConceptInstances()));
	}

	@Override
	public void describeObject(JsonSerializer serializer, Object object, JsonObject result, boolean detailed) {
		// TODO: ask Jean-Charles: i don't understand this
		/*if (object instanceof View) {
			View view = (View) object;
			describeFlexoConceptInstance(serializer, view, result, detailed);
			result.put("virtualModelInstances", serializer.toReferenceArray(view.getVirtualModelInstances()));
		
		}
		else*/ if (object instanceof FMLRTVirtualModelInstance) {
			FMLRTVirtualModelInstance instance = (FMLRTVirtualModelInstance) object;
			describeFlexoConceptInstance(serializer, instance, result, detailed);
			result.put("flexoConceptInstances", serializer.toArray(instance.getFlexoConceptInstances(), detailed));

		}
		else if (object instanceof FlexoConceptInstance) {
			describeFlexoConceptInstance(serializer, (FlexoConceptInstance) object, result, detailed);

		}
		else if (object instanceof ActorReference) {
			ActorReference actorReference = (ActorReference) object;
			result.put("instance", serializer.toReference(actorReference.getFlexoConceptInstance()));
			result.put("value", serializer.toJson(actorReference.getModellingElement(), detailed));
			result.put("roleName", actorReference.getRoleName());
		}
	}
}
