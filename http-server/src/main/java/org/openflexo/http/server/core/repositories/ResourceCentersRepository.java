package org.openflexo.http.server.core.repositories;

import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.http.server.util.IdUtils;

/**
 * A repository for managing the Resource center resource.
 *
 * @author Ihab Benamer
 */
public class ResourceCentersRepository {

    /**
     * It takes a project loader and a resource center id, and returns the resource center with that id
     *
     * @param projectLoader the ProjectLoader instance
     * @param rcid the id of the resource center to retrieve
     * @return A FlexoResourceCenter
     */
    public static FlexoResourceCenter<?> getResourceCenterById(ProjectLoader projectLoader, String rcid){
        return projectLoader.getServiceManager().getResourceCenterService().getFlexoResourceCenter(IdUtils.decodeId(rcid));
    }

}
