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

import java.util.ArrayList;
import java.util.List;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceRepository;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.http.server.HttpService;
import org.openflexo.http.server.core.TechnologyAdapterRouteComplement;
import org.openflexo.http.server.core.TechnologyAdapterRouteService;
import org.openflexo.http.server.util.PamelaResourceRestService;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.technologyadapter.gina.GINATechnologyAdapter;
import org.openflexo.technologyadapter.gina.rm.GINAFIBComponentResource;

public class GinaRouteComplement implements TechnologyAdapterRouteComplement {

	private GINATechnologyAdapter technologyAdapter;

	@Override
	public Class<GINATechnologyAdapter> getTechnologyAdapterClass() {
		return GINATechnologyAdapter.class;
	}

	@Override
	public void initialize(HttpService service, TechnologyAdapterRouteService technologyAdapterRouteService) throws Exception {
		TechnologyAdapterService technologyAdapterService = service.getServiceManager().getTechnologyAdapterService();
		this.technologyAdapter = technologyAdapterService.getTechnologyAdapter(getTechnologyAdapterClass());

		/* TODO Not used yet since fibs and inspectors are in adapter UIs
		JsonSerializer serializer = technologyAdapterRouteService.getSerializer();
		ApplicationFIBRestService applicationFIBRestService = new ApplicationFIBRestService("/application", serializer, technologyAdapterService);
		technologyAdapterRouteService.registerPamelaResourceRestService(technologyAdapter, applicationFIBRestService);
		*/

		PamelaResourceRestService fibService = new PamelaResourceRestService<>(
			"/fib", this::getResources, this::getGinaResource,
			GINAFIBComponentResource.class, technologyAdapterRouteService,
			PamelaMetaModelLibrary.retrieveMetaModel(FIBComponent.class)
		);
		technologyAdapterRouteService.registerPamelaResourceRestService(technologyAdapter, fibService);
	}

	private List<GINAFIBComponentResource> getResources() {
		ArrayList<GINAFIBComponentResource> result = new ArrayList<>();

		for (ResourceRepository<?, Object> repository : technologyAdapter.getAllRepositories()) {
			for (FlexoResource<?> resource : repository.getAllResources()) {
				if (resource instanceof GINAFIBComponentResource) {
					result.add((GINAFIBComponentResource) resource);
				}
			}
		}
		return result;
	}

	private GINAFIBComponentResource getGinaResource(String uri) {
		for (ResourceRepository<?, Object> repository : technologyAdapter.getAllRepositories()) {
			FlexoResource<?> resource = repository.getResource(uri);
			if (resource instanceof GINAFIBComponentResource) {
				return (GINAFIBComponentResource) resource;
			}
		}
		return null;
	}

}
