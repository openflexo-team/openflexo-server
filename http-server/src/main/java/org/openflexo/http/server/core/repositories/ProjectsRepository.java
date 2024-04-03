package org.openflexo.http.server.core.repositories;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.http.server.util.IdUtils;

/**
 *  A repository for managing the Project resource.
 *
 * @author Ihab Benamer
 */
public class ProjectsRepository {

    /**
     * It returns the project from a given id
     *
     * @param virtualModelLibrary the VirtualModelLibrary instance
     * @param projectId the id of the project to load
     * @return A FlexoProject
     */
    public static FlexoProject<?> getProjectById(VirtualModelLibrary virtualModelLibrary, String projectId) {
        FlexoProject<?> project = null;

        for (FlexoProject<?> prj : virtualModelLibrary.getServiceManager().getProjectLoaderService().getRootProjects()) {
            if (prj.getProjectURI().equals(IdUtils.decodeId(projectId))) {
                project = prj;
            }
        }

        return project;
    }

}
