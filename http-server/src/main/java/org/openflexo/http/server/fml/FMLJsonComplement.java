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

import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.http.server.json.JsonComplement;
import org.openflexo.http.server.json.JsonSerializer;
import io.vertx.core.json.JsonObject;

/**
 * Specific JSON complement for FML Rest services
 */
public class FMLJsonComplement implements JsonComplement {

	private static void describeFlexoConcept(JsonSerializer serializer, FlexoConcept flexoConcept, JsonObject result, boolean detailed) {
		result.put("description", serializer.toJson(flexoConcept.getDescription(), detailed));
		result.put("uri",  serializer.toJson(flexoConcept.getURI(), detailed));
		result.put("virtualModel", serializer.toReference(flexoConcept.getOwner()));
		result.put("container", serializer.toReference(flexoConcept.getContainerFlexoConcept()));
		result.put("childFlexoConcepts", serializer.toReferenceArray(flexoConcept.getChildFlexoConcepts()));
		result.put("parents", serializer.toReferenceArray(flexoConcept.getParentFlexoConcepts()));
		result.put("properties", serializer.toMap(flexoConcept.getDeclaredProperties(), FlexoProperty::getName, detailed));
		result.put("behaviors", serializer.toMap(flexoConcept.getDeclaredFlexoBehaviours(), FlexoBehaviour::getName, detailed));
	}
	
	
	private static void describeFlexoEnum(JsonSerializer serializer, FlexoEnum flexoEnum, JsonObject result, boolean detailed) {
		result.put("values", serializer.toJson(flexoEnum.getValues(), detailed));
	}
	

	@Override
	public void describeObject(JsonSerializer serializer, Object object, JsonObject result, boolean detailed) {
		// TODO: ask Jean-Charles: i don't understand this
		/*if (object instanceof ViewPoint) {
			ViewPoint viewPoint = (ViewPoint) object;
			describeFlexoConcept(serializer, viewPoint, result, detailed);
			result.put("virtualModels", serializer.toReferenceArray(viewPoint.getVirtualModels()));
		
		} else*/ if (object instanceof VirtualModel) {
			VirtualModel model = (VirtualModel) object;
			describeFlexoConcept(serializer, model, result, detailed);			
			result.put("flexoConcepts", serializer.toReferenceArray(model.getFlexoConcepts()));
			
			if (object instanceof FlexoEnum) {
				FlexoEnum flexoEnum = (FlexoEnum) object;
				describeFlexoEnum(serializer, flexoEnum, result, detailed);
			}

		}
		else if (object instanceof FlexoConcept) {
			FlexoConcept concept = (FlexoConcept) object;
			describeFlexoConcept(serializer, concept, result, detailed);
			
			if (object instanceof FlexoEnum) {
				FlexoEnum flexoEnum = (FlexoEnum) object;
				describeFlexoEnum(serializer, flexoEnum, result, detailed);

			}

		}
		else if (object instanceof FlexoRole) {
			FlexoRole role = (FlexoRole) object;
			result.put("cardinality", serializer.toJson(role.getCardinality(), detailed));
			result.put("declaredType", serializer.toJson(role.getType(), detailed));
			result.put("flexoConcept", serializer.toReference(role.getFlexoConcept()));
			
			if (object instanceof FMLRTVirtualModelInstanceModelSlot) {
				FMLRTVirtualModelInstanceModelSlot modelSlot = (FMLRTVirtualModelInstanceModelSlot) object;
				result.put("accessedVirtualModel", serializer.toJson(modelSlot.getAccessedVirtualModel().getName(), detailed));				
			}

		}
		else if (object instanceof FlexoBehaviour) {
			FlexoBehaviour behavior = (FlexoBehaviour) object;
			result.put("parameters", serializer.toArray(behavior.getParameters(), detailed));
			result.put("returnType", serializer.toJson(behavior.getReturnType(), detailed));
			result.put("flexoConcept", serializer.toReference(behavior.getFlexoConcept()));
			result.put("controlGraph", serializer.toArray(behavior.getControlGraph().getFlattenedSequence(), detailed));

		}
		else if (object instanceof FlexoBehaviourParameter) {
			FlexoBehaviourParameter parameter = (FlexoBehaviourParameter) object;
			result.put("declaredType", serializer.toJson(parameter.getType(), detailed));
			result.put("behavior", serializer.toReference(parameter.getBehaviour()));
			result.put("flexoConcept", serializer.toReference(parameter.getFlexoConcept()));
		}
	}
}
