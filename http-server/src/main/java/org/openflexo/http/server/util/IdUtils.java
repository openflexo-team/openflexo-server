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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.http.server.core.TechnologyAdapterRouteService;

/**
 * Created by charlie on 07/02/2017.
 */
public class IdUtils {

	private final static Encoder encoder = Base64.getUrlEncoder().withoutPadding();
	private final static Decoder urlDecoder = Base64.getUrlDecoder();

	public static String getId(Object object) {
		if (object instanceof FlexoResource) {
			return IdUtils.encodeuri(((FlexoResource<?>) object).getURI());
		}
		if (object instanceof FlexoObject) {
			long flexoID = ((FlexoObject) object).getFlexoID();
			return flexoID >= 0 ? Long.toString(flexoID) : null;
		}
		return null;
	}

	public static String getUrl(Object object, TechnologyAdapterRouteService service) {
		FlexoResource<?> resource = null;
		long id = -1;

		if (object instanceof FlexoResource) {
			resource = (FlexoResource<?>) object;
		}
		else if (object instanceof ResourceData) {
			resource = ((ResourceData<?>) object).getResource();
		}
		else if (object instanceof InnerResourceData) {
			ResourceData<?> resourceData = ((InnerResourceData<?>) object).getResourceData();
			resource = resourceData != null ? resourceData.getResource() : null;
		}

		if (object instanceof FlexoObject) {
			id = ((FlexoObject) object).getFlexoID();
		}

		if (resource == null)
			return null;

		if (id >= 0) {
			String prefix = service.getPrefix(resource);
			if (prefix == null)
				return null;

			StringBuilder url = new StringBuilder();
			url.append(prefix);
			url.append("/");
			url.append(IdUtils.encodeuri(resource.getURI()));
			if (id >= 0) {
				url.append("/object/");
				url.append(id);
			}

			return url.toString();
		}
		StringBuilder url = new StringBuilder();
		url.append("/resource/");
		url.append(IdUtils.encodeuri(resource.getURI()));
		return url.toString();
	}

	public static String encodeuri(String uri) {
		return encoder.encodeToString(uri.getBytes(StandardCharsets.ISO_8859_1));
	}

	public static String decodeId(String encoded) {
		return new String(urlDecoder.decode(encoded), StandardCharsets.ISO_8859_1);
	}

	public static String decodeUrlSpecialCharacters(String url) {
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			/* can't happen */
			return url;
		}
	}

	private static Set<Character> validCharacters = Stream
			.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
					'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
					'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '$', '-', '_', '.', '+', '!', '*', '\'', '(', ')')
			.collect(Collectors.toSet());

	/**
	 * Cleans given raw id to construct a sanitized id for REST apis. Only Alphanumerics [0-9a-zA-Z] and special characters $-_.+!*'(), are
	 * allowed.
	 *
	 * A raw id will always give the same sanitized id but it's not possible to reconstruct the raw id from the sanitized one.
	 *
	 * @param rawId
	 *            id to clean, cant be null
	 * @return a sanitized id, never null,never empty
	 * @throws NullPointerException
	 *             if raw id is null
	 * @throws IllegalArgumentException
	 *             if clean id is empty.
	 */
	public static String sanitiseId(String rawId) {
		if (rawId == null)
			throw new NullPointerException();

		StringBuilder id = new StringBuilder();
		for (int i = 0; i < rawId.length(); i++) {
			char current = rawId.charAt(i);
			if (validCharacters.contains(current)) {
				id.append(Character.toLowerCase(current));
			}
		}
		if (id.length() == 0)
			throw new IllegalArgumentException("Sanitized id is empty from '" + rawId + "'");
		return id.toString();
	}

	public static String getTechnologyAdapterId(TechnologyAdapter<?> adapter) {
		return sanitiseId(adapter.getIdentifier());
	}

}
