package org.openflexo.http.server.fml;

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.http.server.HttpService;
import org.openflexo.http.server.core.TechnologyAdapterRouteComplement;
import org.openflexo.http.server.core.TechnologyAdapterRouteService;
import org.openflexo.http.server.util.PamelaResourceRestService;
import org.openflexo.pamela.PamelaMetaModelLibrary;

/**
 * Route complements for FML technology adapter
 */
public class FMLRouteComplement implements TechnologyAdapterRouteComplement {

	private FMLTechnologyAdapter technologyAdapter;

	@Override
	public Class<FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	@Override
	public void initialize(HttpService service, TechnologyAdapterRouteService taRouteService) throws Exception {
		technologyAdapter = service.getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(getTechnologyAdapterClass());
		VirtualModelLibrary virtualModelLibrary = technologyAdapter.getVirtualModelLibrary();
		TechnologyAdapterRouteService taService = service.getTechnologyAdapterRestService();

		// Adds pamela rest service for VirtualModels resources
		PamelaResourceRestService<FMLCompilationUnit, CompilationUnitResource> viewPointConverter = new PamelaResourceRestService<>(
				"/model", virtualModelLibrary::getCompilationUnitResources, virtualModelLibrary::getCompilationUnitResource,
				CompilationUnitResource.class, taService, PamelaMetaModelLibrary.retrieveMetaModel(VirtualModel.class));
		taRouteService.registerPamelaResourceRestService(technologyAdapter, viewPointConverter);
	}

}
