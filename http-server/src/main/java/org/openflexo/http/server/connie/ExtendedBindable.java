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

import java.util.Map;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DefaultBindable;
import org.openflexo.connie.java.JavaBindingFactory;
import org.openflexo.foundation.fml.FMLBindingFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;

public class ExtendedBindable extends DefaultBindable {

	private final Bindable parentBindable;

	private final BindingModel model;

	private final BindingFactory factory;

	public ExtendedBindable(Bindable parentBindable, Map<String, Object> objects) {
		this.parentBindable = parentBindable;
		this.factory = createFactory(parentBindable);

		this.model = new BindingModel(parentBindable != null ? parentBindable.getBindingModel() : null);
		for (String variableName : objects.keySet()) {
			Object value = objects.get(variableName);
			model.addToBindingVariables(new BindingVariable(variableName, getBindingFactory().getTypeForObject(value)));
		}
	}

	private static BindingFactory createFactory(Bindable parentBindable) {
		if (parentBindable instanceof FMLObject) {
			return new FMLBindingFactory(((FMLObject) parentBindable).getDeclaringCompilationUnit().getVirtualModel());
		}
		if (parentBindable instanceof FlexoConceptInstance) {
			return new FMLBindingFactory(
					((FlexoConceptInstance) parentBindable).getFlexoConcept().getDeclaringCompilationUnit().getVirtualModel());
		}
		return new JavaBindingFactory();
	}

	@Override
	public void notifiedBindingChanged(DataBinding<?> dataBinding) {
		if (parentBindable != null) {
			parentBindable.notifiedBindingChanged(dataBinding);
		}
	}

	@Override
	public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
		if (parentBindable != null) {
			parentBindable.notifiedBindingDecoded(dataBinding);
		}
	}

	@Override
	public BindingModel getBindingModel() {
		return model;
	}

	@Override
	public BindingFactory getBindingFactory() {
		return factory;
	}
}
