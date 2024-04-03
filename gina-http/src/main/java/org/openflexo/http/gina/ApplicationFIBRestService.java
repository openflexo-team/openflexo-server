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

package org.openflexo.http.gina;

import java.util.Collection;
import java.util.Collections;

import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.gina.ApplicationFIBLibrary;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.http.server.json.JsonSerializer;
import org.openflexo.http.server.util.ResourceRestService;
import org.openflexo.rm.Resource;

/**
 * Creates REST entry point to serveRoot Pamela models of a given root type.
 *
 * TODO Currently not in use since application fibs and inspectors are in UI parts of the adaptors
 */
public class ApplicationFIBRestService extends ResourceRestService<FIBComponent, Resource> {

	private final ApplicationFIBLibrary library;

	public ApplicationFIBRestService(String prefix, JsonSerializer serializer, TechnologyAdapterService service) {
		super(prefix, Resource.class, serializer);
		library = ApplicationFIBLibrary.ApplicationFIBLibraryImpl.instance(service);
	}

	@Override
	protected Collection<Resource> allResources() {
		return Collections.emptyList();
	}

	@Override
	protected Resource findResource(String id) {
		// TODO find resource
		return null;
	}

	@Override
	protected FIBComponent loadResource(Resource resource) throws Exception {
		return library.retrieveFIBComponent(resource, true);
	}

	@Override
	protected Collection<Object> allObjects(Resource resource) {
		return Collections.singleton(library.retrieveFIBComponent(resource, true));
	}

	@Override
	protected Object findObject(Resource resource, String id) {
		// TODO Check id
		return library.retrieveFIBComponent(resource, true);
	}
}
