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

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

/**
 * TypescriptModelFromPamela generates a TypeScript description for a Pamela model.
 */
public class TypescriptModelFromPamela {

	private final Set<String> ignoredNames = Stream.of("name").collect(Collectors.toSet());
	private final PamelaMetaModel context;

	public TypescriptModelFromPamela(PamelaMetaModel context) {
		this.context = context;
	}

	public String generateTypeScript() {
		StringBuilder result = new StringBuilder();

		result.append("import { Description } from \"./general\";\n\n");

		Iterator<ModelEntity> entities = context.getEntities();
		while (entities.hasNext()) {
			try {
				result.append(generateTypeScriptInterface(entities.next()));
				result.append("\n\n");
			} catch (ModelDefinitionException e) {
				// ouch, too bad no model no typescript ...
			}
		}
		return result.toString();
	}

	private final static boolean isReference(ModelProperty<?> property) {
		if (property.getCloningStrategy() == CloningStrategy.StrategyType.IGNORE)
			return true;
		return property.getEmbedded() == null && property.getComplexEmbedded() != null && property.getXMLElement() == null;
	}

	private final boolean ignoreProperty(String propertyName, ModelProperty<?> property) {
		return ignoredNames.contains(propertyName) && propertyName != null && !property.ignoreType();
	}

	private String generateTypeScriptInterface(ModelEntity<Object> entity) throws ModelDefinitionException {
		StringBuilder tsInterface = new StringBuilder();
		tsInterface.append("export interface ");
		tsInterface.append(entity.getXMLTag());
		List<?> directSuperEntities = entity.getDirectSuperEntities();
		tsInterface.append(" extends ");
		List<ModelEntity<?>> superEntities = (List<ModelEntity<?>>) directSuperEntities;
		if (superEntities != null && !superEntities.isEmpty()) {
			StringJoiner joiner = new StringJoiner(", ");
			for (ModelEntity<?> superEntity : superEntities) {
				joiner.add(superEntity.getXMLTag());
			}
			tsInterface.append(joiner.toString());
		}
		else {
			tsInterface.append("Description<");
			tsInterface.append(entity.getXMLTag());
			tsInterface.append(">");
		}
		tsInterface.append(" {\n");

		// adds kind attribute
		List<ModelEntity> descendants = entity.getAllDescendants(context);
		if (!entity.isAbstract())
			descendants.add(entity);
		if (descendants.size() > 0) {
			tsInterface.append("\treadonly kind: ");
			String kinds = descendants.stream().map((e) -> '"' + e.getXMLTag() + '"').collect(Collectors.joining("|"));

			tsInterface.append(kinds);
			tsInterface.append(";\n");
		}

		for (ModelProperty<?> property : entity.getDeclaredProperties()) {
			XMLAttribute xmlAttribute = property.getXMLAttribute();
			XMLElement xmlElement = property.getXMLElement();

			String propertyName = property.getPropertyIdentifier();
			if (xmlAttribute != null && xmlAttribute.xmlTag().length() > 0) {
				propertyName = xmlAttribute.xmlTag();
			}
			if (xmlElement != null && xmlElement.xmlTag().length() > 0) {
				propertyName = xmlElement.xmlTag();
			}

			if (!ignoreProperty(propertyName, property)) {
				tsInterface.append("\treadonly ");
				tsInterface.append(propertyName);
				tsInterface.append(": ");
				tsInterface.append(getTsTypeName(property));
				tsInterface.append(";\n");
			}
		}

		tsInterface.append("}");
		return tsInterface.toString();
	}

	private static String getTsTypeName(ModelProperty<?> property) {
		String result = "any";
		Class<?> type = property.getType();
		if (isStringType(property)) {
			result = "string";
		}
		else if (Number.class.isAssignableFrom(type) || TypeUtils.isNumber(type)) {
			result = "number";
		}
		else if (TypeUtils.isBoolean(type)) {
			result = "boolean";
		}
		else if (type.isEnum()) {
			StringJoiner joiner = new StringJoiner(" | ");
			for (Object constant : type.getEnumConstants()) {
				joiner.add("\"" + constant.toString() + "\"");
			}
			result = joiner.toString();
		}
		else {
			ModelEntity<?> entity = property.getAccessedEntity();
			if (entity != null) {
				result = entity.getXMLTag();
			}
			if (isReference(property)) {
				result = "Description<" + result + ">";
			}
		}

		if (property.getCardinality() == Getter.Cardinality.LIST) {
			result = "Array<" + result + ">";
		}

		return result;
	}

	private static boolean isStringType(ModelProperty<?> property) {
		Class<?> type = property.getType();
		return type == DataBinding.class || type == Class.class || type == Color.class || type == Font.class || type == File.class
				|| property.isStringConvertable() || type == String.class;
	}
}
