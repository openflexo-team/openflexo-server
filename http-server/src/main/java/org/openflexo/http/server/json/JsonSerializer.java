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

package org.openflexo.http.server.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.http.server.core.TechnologyAdapterRouteService;
import org.openflexo.http.server.util.IdUtils;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.factory.StringEncoder;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import javassist.util.proxy.ProxyObject;

/**
 * An instance of JsonSerializer transforms Pamela object to JSON object. The JSON result only contains the object destined to a REST
 * service.
 */
public class JsonSerializer {

	/** Cloning strategies that actually clone objects */
	private final Set<CloningStrategy.StrategyType> cloningStrategies = Stream
			.of(CloningStrategy.StrategyType.CLONE, CloningStrategy.StrategyType.CUSTOM_CLONE).collect(Collectors.toSet());

	private final TechnologyAdapterRouteService service;

	private final List<JsonComplement> complements = new ArrayList<>();

	private final List<StringEncoder> encoders = new ArrayList<>();

	public JsonSerializer(TechnologyAdapterRouteService service) {
		this.service = service;
		initialize();
	}

	private void initialize() {
		// gets complements
		ServiceLoader<JsonComplement> foundComplements = ServiceLoader.load(JsonComplement.class);
		for (JsonComplement complement : foundComplements) {
			complements.add(complement);

			try {
				PamelaModelFactory factory = complement.getFactory();
				if (factory != null && factory.getStringEncoder() != null) {
					encoders.add(factory.getStringEncoder());
				}
			} catch (ModelDefinitionException e) {
				// nothing to do
			}
		}

	}

	private StringEncoder findEncoder(Class<?> clazz) {
		for (StringEncoder encoder : encoders) {
			if (encoder.isConvertable(clazz))
				return encoder;
		}
		return null;
	}

	public Object toJson(Object object, boolean detailed) {
		return toJson(object, false, detailed);
	}

	public Object toReference(Object object) {
		return toJson(object, true, false);
	}

	protected Object toJson(Object object, boolean reference, boolean detailed) {
		if (object == null)
			return null;

		Class<?> type = object.getClass();
		if (TypeUtils.kindOfType(type).getType() != Object.class) {
			// no transformation needed
			return object;
		}
		else if (findEncoder(object.getClass()) != null) {
			try {
				StringEncoder encoder = findEncoder(object.getClass());
				return encoder.toString(object);
			} catch (InvalidDataException e) {
				return null;
			}
		}
		else if (type.isEnum() || type.getSuperclass().isEnum()) {
			return object.toString();
		}
		else if (object instanceof Collection) {
			if (reference) {
				return toReferenceArray((Collection<?>) object);
			}
			return toArray((Collection<?>) object, detailed);
		}
		else if (object instanceof FlexoResource) {
			return JsonUtils.getResourceDescription((FlexoResource<?>) object, service);

		}
		else {
			JsonObject result = new JsonObject();
			identifyObject(object, result);
			for (JsonComplement complement : complements) {
				complement.identifyObject(this, object, result);
			}
			if (!reference) {
				describeObject(object, result, detailed);
				for (JsonComplement complement : complements) {
					complement.describeObject(this, object, result, detailed);
				}
			}
			return result;
		}
	}

	public <T> JsonObject toMap(Collection<T> list, Function<T, String> keyFunction, boolean detailed) {
		JsonObject result = new JsonObject();
		for (T t : list) {
			result.put(keyFunction.apply(t), toJson(t, detailed));
		}
		return result;
	}

	public <T> JsonObject toReferenceMap(Collection<T> list, Function<T, String> keyFunction) {
		JsonObject result = new JsonObject();
		for (T t : list) {
			result.put(keyFunction.apply(t), toReference(t));
		}
		return result;
	}

	public JsonArray toArray(Collection<?> list, boolean detailed) {
		return new JsonArray(list.stream().map((i) -> toJson(i, detailed)).collect(Collectors.toList()));
	}

	public JsonArray toReferenceArray(Collection<?> list) {
		return new JsonArray(list.stream().map((i) -> toJson(i, true, false)).collect(Collectors.toList()));
	}

	private boolean identifyObject(Object object, JsonObject result) {
		String id = IdUtils.getId(object);

		if (object instanceof FMLObject) {
			result.put("name", ((FMLObject) object).getName());
		}
		if (object instanceof FlexoConcept) {
			result.put("uri", ((FlexoConcept) object).getURI());
		}
		if (object instanceof FlexoBehaviour) {
			result.put("uri", ((FlexoBehaviour) object).getURI());
		}

		if (id != null) {
			result.put("id", id);
			String url = IdUtils.getUrl(object, service);
			if (url != null) {
				result.put("url", url);
			}
			// Used for debugging purposes
			// result.put("__debug_object__", object.toString());
		}
		result.put("kind", getType(object));

		if(object instanceof FlexoConceptInstance) {
			FlexoConceptInstance fci  = (FlexoConceptInstance) object;
			result.put("type", fci.getFlexoConcept().getName());
		}


		return id != null;
	}

	private static String getType(Object object) {
		if (object instanceof ProxyObject) {
			ProxyMethodHandler<?> handler = (ProxyMethodHandler<?>) ((ProxyObject) object).getHandler();
			@SuppressWarnings({ "unchecked" })
			ModelEntity<Object> modelEntity = (ModelEntity<Object>) handler.getModelEntity();
			return modelEntity.getXMLTag();
		}
		return object.getClass().getSimpleName();
	}

	private final boolean isReference(ModelProperty<?> property) {
		if (!cloningStrategies.contains(property.getCloningStrategy()))
			return true;
		return property.getEmbedded() == null && property.getComplexEmbedded() != null && property.getXMLElement() == null;
	}

	public void describeObject(Object object, JsonObject result, boolean detailed) {

		// adds resource link
		if (object instanceof ResourceData) {
			FlexoResource<?> resource = ((ResourceData<?>) object).getResource();
			String resourceUrl = IdUtils.getUrl(resource, service);
			if (resourceUrl != null) {
				result.put("resourceUrl", resourceUrl);
			}
		}

		if (object instanceof ProxyObject) {
			ProxyMethodHandler<?> handler = (ProxyMethodHandler<?>) ((ProxyObject) object).getHandler();
			@SuppressWarnings({ "unchecked" })
			ModelEntity<Object> modelEntity = (ModelEntity<Object>) handler.getModelEntity();

			try {
				if (detailed) {
					Iterator<ModelProperty<? super Object>> iterator = modelEntity.getProperties();
					while (iterator.hasNext()) {
						ModelProperty property = iterator.next();

						XMLAttribute xmlAttribute = property.getXMLAttribute();
						XMLElement xmlElement = property.getXMLElement();

						Object propertyValue = handler.invokeGetter(property);
						Object transformed = null;

						// avoid source
						if (propertyValue == object)
							continue;

						String propertyName = property.getPropertyIdentifier();
						if (xmlAttribute != null && xmlAttribute.xmlTag().length() > 0) {
							propertyName = xmlAttribute.xmlTag();
						}
						if (xmlElement != null && xmlElement.xmlTag().length() > 0) {
							propertyName = xmlElement.xmlTag();
						}

						if (propertyName != null && !result.containsKey(propertyName)) {

							boolean reference = isReference(property);
							switch (property.getCardinality()) {
								case SINGLE: {
									transformed = toJson(propertyValue, reference, detailed);
									break;
								}
								case LIST: {
									List<Object> collected = new ArrayList<>();
									for (Object child : (List<?>) propertyValue) {
										collected.add(toJson(child, reference, detailed));
									}
									if (!collected.isEmpty()) {
										transformed = new JsonArray(collected);
									}
									break;
								}
								default:
									break;
							}

							if (transformed != null) {
								result.put(propertyName, transformed);
							}
						}
					}
				}
			} catch (ModelDefinitionException e) {
				// nothing to do
			}
		}
	}
}