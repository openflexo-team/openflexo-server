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

package org.openflexo.http.server.util;

import io.vertx.core.json.JsonObject;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceRepository;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResourceRepository;
import org.openflexo.http.server.core.ta.TechnologyAdapterRestComplement;
import org.openflexo.http.server.core.ta.TechnologyAdapterRestService;

/**
 * Utility methods for JSON handling
 */
public class JsonUtils {

	public static JsonObject getCenterDescription(FlexoResourceCenter<?> center) {
		String uri = center.getDefaultBaseURI();
		String id = IdUtils.encoreUri(uri);
		JsonObject centerDescription = new JsonObject();
		centerDescription.put("name", center.getDisplayableName());
		centerDescription.put("type", "ResourceCenter");
		centerDescription.put("uri", uri);
		centerDescription.put("id", id);
		centerDescription.put("url", "/rc/" + id);
		centerDescription.put("resourceUrl", "/rc/" + id + "/resource");
		return centerDescription;
	}

	public static JsonObject getRepositoryDescription(ResourceRepository<?,?> repository) {
		String uri = repository.getDefaultBaseURI();
		String id = IdUtils.encoreUri(uri);
		JsonObject description = new JsonObject();
		description.put("name", repository.getDisplayableName());
		description.put("type", "ResourceRepository");
		description.put("uri", uri);
		description.put("id", id);
		description.put("url", "/rc/" + id);
		description.put("resourceUrl", "/rc/" + id + "/resource");
		if (repository instanceof TechnologyAdapterResourceRepository) {
			String taId = IdUtils.getTechnologyAdapterId(((TechnologyAdapterResourceRepository) repository).getTechnologyAdapter());
			description.put("technologyAdapterId", taId);
			description.put("technologyAdapterUrl", "/ta/"+taId);
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

	public static JsonObject getResourceDescription(FlexoResource<?> resource, TechnologyAdapterRestService service) {
		String uri = resource.getURI();
		String id = IdUtils.encoreUri(uri);
		JsonObject resourceDescription = new JsonObject();
		resourceDescription.put("name", resource.getName());
		resourceDescription.put("type", "Resource");
		resourceDescription.put("uri", uri);
		resourceDescription.put("id", id);
		if (resource.getResourceCenter() != null) {
			String centerId = IdUtils.encoreUri(resource.getResourceCenter().getDefaultBaseURI());
			resourceDescription.put("resourceCenterId", centerId);
			resourceDescription.put("resourceCenterUrl", "/rc/" + centerId);
		} else {
			System.out.println("Resource '"+ resource.getName() + "' has no rc.");
		}
		resourceDescription.put("url", "/resource/" + id);
		resourceDescription.put("contentUrl", "/resource/" + id + "/contents");
		if (resource instanceof TechnologyAdapterResource) {
			TechnologyAdapterResource technologyAdapterResource = (TechnologyAdapterResource) resource;
			TechnologyAdapter technologyAdapter = technologyAdapterResource.getTechnologyAdapter();
			String taId = IdUtils.getTechnologyAdapterId(technologyAdapter);
			resourceDescription.put("technologyAdapterId", taId);
			resourceDescription.put("technologyAdapterUrl", "/ta/"+taId);

			TechnologyAdapterRestComplement complement = service.getComplement(technologyAdapter);
			if (complement != null) {
				complement.complementResource(technologyAdapterResource, resourceDescription);
			}
		}
		return resourceDescription;
	}

	public static JsonObject getTechnologyAdapterDescription(String id, TechnologyAdapter adapter, TechnologyAdapterRestComplement complement) {
		JsonObject resourceDescription = new JsonObject();
		resourceDescription.put("name", adapter.getName());
		resourceDescription.put("type", "TechnologyAdapter");

		resourceDescription.put("id", id);
		resourceDescription.put("activated", adapter.isActivated());

		String url = "/ta/" + id;
		resourceDescription.put("url", url);
		resourceDescription.put("complemented", complement != null);
		if (complement != null) {
			complement.complementRoot(url, resourceDescription);
		}
		return resourceDescription;
	}

}
