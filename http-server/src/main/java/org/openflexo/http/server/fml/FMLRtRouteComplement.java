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

import java.util.ArrayList;
import java.util.List;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.http.server.HttpService;
import org.openflexo.http.server.core.TechnologyAdapterRouteComplement;
import org.openflexo.http.server.core.TechnologyAdapterRouteService;
import org.openflexo.http.server.util.PamelaResourceRestService;
import org.openflexo.pamela.PamelaMetaModelLibrary;

/**
 * Route complements for FML@RT technology adapter
 */
public class FMLRtRouteComplement implements TechnologyAdapterRouteComplement {

	private FMLRTTechnologyAdapter technologyAdapter;

	@Override
	public Class<FMLRTTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLRTTechnologyAdapter.class;
	}

	@Override
	public void initialize(HttpService service, TechnologyAdapterRouteService taRouteService) throws Exception {
		technologyAdapter = service.getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(getTechnologyAdapterClass());
		TechnologyAdapterRouteService taService = service.getTechnologyAdapterRestService();

		// Adds pamela rest service for virtual model instance resources
		PamelaResourceRestService<FMLRTVirtualModelInstance, FMLRTVirtualModelInstanceResource> viewConverter = new PamelaResourceRestService<>(
				"/instance", this::getVirtualModelInstanceResources, this::getVirtualModelInstanceResource,
				FMLRTVirtualModelInstanceResource.class, taService,
				PamelaMetaModelLibrary.retrieveMetaModel(FMLRTVirtualModelInstance.class));
		taRouteService.registerPamelaResourceRestService(technologyAdapter, viewConverter);

	}

	private List<FMLRTVirtualModelInstanceResource> getVirtualModelInstanceResources() {
		ArrayList<FMLRTVirtualModelInstanceResource> result = new ArrayList<>();
		for (FMLRTVirtualModelInstanceRepository<?> viewLibrary : technologyAdapter.getVirtualModelInstanceRepositories()) {
			result.addAll(viewLibrary.getAllResources());
		}
		return result;
	}

	private FMLRTVirtualModelInstanceResource getVirtualModelInstanceResource(String uri) {
		for (FMLRTVirtualModelInstanceRepository<?> viewLibrary : technologyAdapter.getVirtualModelInstanceRepositories()) {
			AbstractVirtualModelInstanceResource<?, ?> virtualModelInstance = viewLibrary.getVirtualModelInstance(uri);
			if (virtualModelInstance instanceof FMLRTVirtualModelInstanceResource) {
				return (FMLRTVirtualModelInstanceResource) virtualModelInstance;
			}
		}
		return null;
	}
}
