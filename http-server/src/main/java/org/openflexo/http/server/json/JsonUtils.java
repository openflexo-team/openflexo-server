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

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceRepository;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResourceRepository;
import org.openflexo.http.server.core.TechnologyAdapterRouteComplement;
import org.openflexo.http.server.core.TechnologyAdapterRouteService;
import org.openflexo.http.server.util.IdUtils;
import org.openflexo.http.server.util.ResourceRestService;
import org.openflexo.http.server.util.ResourceUtils;

import io.vertx.core.json.JsonObject;
import org.openflexo.toolbox.StringUtils;

import java.util.*;

/**
 * Utility methods for JSON handling
 */
public class JsonUtils {

	/** Map of technology adapters to corresponding {@link TechnologyAdapterRouteComplement} */
	private final static Map<TechnologyAdapter<?>, TechnologyAdapterRouteComplement> complementMap = new LinkedHashMap<>();

	/** Map of registered {@link org.openflexo.http.server.util.PamelaResourceRestService}s for each technology adapters */
	private final static Map<TechnologyAdapter<?>, List<ResourceRestService>> restServices = new HashMap<>();

	public static JsonObject getCenterDescription(FlexoResourceCenter<?> center) {
		String uri = center.getDefaultBaseURI();
		String id = IdUtils.encodeuri(uri);
		JsonObject centerDescription = new JsonObject();
		centerDescription.put("name", center.getDisplayableName());
		centerDescription.put("type", "ResourceCenter");
		centerDescription.put("uri", uri);
		centerDescription.put("id", id);
		centerDescription.put("url", "/rc/" + id);
		centerDescription.put("resourceUrl", "/rc/" + id + "/resource");
		return centerDescription;
	}

	public static JsonObject getRepositoryDescription(ResourceRepository<?, ?> repository) {
		String uri = repository.getDefaultBaseURI();
		String id = IdUtils.encodeuri(uri);
		JsonObject description = new JsonObject();
		description.put("name", repository.getDisplayableName());
		description.put("type", "ResourceRepository");
		description.put("uri", uri);
		description.put("id", id);
		description.put("url", "/rc/" + id);
		description.put("resourceUrl", "/rc/" + id + "/resource");
		if (repository instanceof TechnologyAdapterResourceRepository) {
			String taId = IdUtils
					.getTechnologyAdapterId(((TechnologyAdapterResourceRepository<?, ?, ?, ?>) repository).getTechnologyAdapter());
			description.put("technologyAdapterId", taId);
			description.put("technologyAdapterUrl", "/ta/" + taId);
		}
		return description;

	}

	public static JsonObject getFolderDescription(String name, String path, String rcId) {
		JsonObject folderDescription = new JsonObject();
		folderDescription.put("name", name);
		folderDescription.put("type", "Folder");
		String id = path + (path.endsWith("/") ? "" : "/") + name;
		folderDescription.put("id", id);

		String rcUrl = "/rc/" + rcId;
		folderDescription.put("url", rcUrl + "/resource" + id);
		folderDescription.put("resourceCenterId", rcId);
		folderDescription.put("resourceCenterUrl", rcUrl);
		return folderDescription;
	}

	public static JsonObject getResourceDescription(FlexoResource<?> resource, TechnologyAdapterRouteService service) {
		String uri = resource.getURI();
		String id = IdUtils.encodeuri(uri);
		JsonObject resourceDescription = new JsonObject();
		resourceDescription.put("name", resource.getName());
		resourceDescription.put("type", "Resource");
		resourceDescription.put("uri", uri);
		resourceDescription.put("id", id);
		resourceDescription.put("modified", resource.isModified());
		resourceDescription.put("loaded", resource.isLoaded());

		if (resource.getResourceCenter() != null) {
			String centerId = IdUtils.encodeuri(resource.getResourceCenter().getDefaultBaseURI());
			resourceDescription.put("resourceCenterId", centerId);
			resourceDescription.put("resourceCenterUrl", "/rc/" + centerId);
		}
		else {
			System.out.println("Resource '" + resource.getName() + "' has no rc.");
		}
		resourceDescription.put("url", "/resource/" + id);
		resourceDescription.put("contentUrl", "/resource/" + id + "/contents");
		if (resource instanceof TechnologyAdapterResource) {
			TechnologyAdapterResource<?, ?> technologyAdapterResource = (TechnologyAdapterResource<?, ?>) resource;
			TechnologyAdapter<?> technologyAdapter = technologyAdapterResource.getTechnologyAdapter();
			String taId = IdUtils.getTechnologyAdapterId(technologyAdapter);
			resourceDescription.put("technologyAdapterId", taId);
			resourceDescription.put("technologyAdapterUrl", "/ta/" + taId);

			String prefix = service.getPrefix(resource);
			if (prefix != null) {
				if (resource.isLoaded()) {
					ResourceData<?> data = ResourceUtils.safeGetResourceOrNull(resource);
					if (data instanceof FlexoObject) {
						String url = IdUtils.getUrl(data, service);
						if (url != null) {
							resourceDescription.put("modelUrl", url);
						}
					}
				}
				resourceDescription.put("objectsUrl", prefix + "/" + id + "/object");
			}
		}
		return resourceDescription;
	}

	public static JsonObject getTechnologyAdapterDescription(String id, TechnologyAdapter<?> adapter) {
		JsonObject result = new JsonObject();
		result.put("name", adapter.getName());
		result.put("type", "TechnologyAdapter");

		result.put("id", id);
		result.put("activated", adapter.isActivated());

		String url = "/ta/" + id;
		result.put("url", url);

		JsonUtils.complementTechnologyAdapter(adapter, result);

		return result;
	}

	public static void complementTechnologyAdapter(TechnologyAdapter<?> adapter, JsonObject result) {
		TechnologyAdapterRouteComplement complement = JsonUtils.complementMap.get(adapter);
		result.put("complemented", complement != null);
		for (ResourceRestService service : JsonUtils.restServices.getOrDefault(adapter, Collections.emptyList())) {
			String simpleName = service.getResourceClass().getSimpleName();
			String route = result.getString("url") + service.getPrefix();
			result.put(StringUtils.firstsLower(simpleName) + "Url", route);
		}
	}

}
