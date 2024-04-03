package org.openflexo.http.server.core.controllers;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import jline.internal.Log;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.action.AddRepositoryFolder;
import org.openflexo.foundation.action.CreateProject;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.resource.*;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.http.server.core.TechnologyAdapterRouteService;
import org.openflexo.http.server.core.helpers.Helpers;
import org.openflexo.http.server.core.repositories.ProjectsRepository;
import org.openflexo.http.server.core.serializers.JsonSerializer;
import org.openflexo.http.server.core.validators.FolderValidator;
import org.openflexo.http.server.core.validators.ProjectsValidator;
import org.openflexo.http.server.json.JsonUtils;
import org.openflexo.http.server.util.IdUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 *  Projects rest apis controller.
 * @author Ihab Benamer
 */
public class ProjectsController extends GenericController {
    private final VirtualModelLibrary virtualModelLibrary;
    private final TechnologyAdapterRouteService technologyAdapterRestService;
    private final ProjectLoader projectLoader;
    private final DefaultFlexoEditor editor;

    /**
     * Instantiates a new Projects controller.
     *
     * @param virtualModelLibrary the virtual model library
     * @param projectLoader the project loader
     * @param technologyAdapterRestService the technology Adapter Rest Service
     */
    public ProjectsController(VirtualModelLibrary virtualModelLibrary, ProjectLoader projectLoader, TechnologyAdapterRouteService technologyAdapterRestService) {
        this.virtualModelLibrary            = virtualModelLibrary;
        this.projectLoader                  = projectLoader;
        this.technologyAdapterRestService   = technologyAdapterRestService;
        editor                              = Helpers.getDefaultFlexoEditor(virtualModelLibrary);
    }

    /**
     * It creates a JSON array, iterates over all the projects in the project loader, and adds a JSON representation of
     * each project to the array
     *
     * @param context the routing context
     */
    public void list(RoutingContext context) {
        JsonArray result = new JsonArray();

        for (FlexoProject<?> project : projectLoader.getRootProjects()) {
            result.add(JsonSerializer.projectSerializer(project));
        }

        context.response().end(result.encodePrettily());
    }

    /**
     * It creates a new project
     *
     * @param context The routing context is a wrapper around the request and response objects. It also contains the
     * request body and the parsed parameters.
     */
    public void add(RoutingContext context) {
        ProjectsValidator validator = new ProjectsValidator(projectLoader, context.request());
        JsonArray errors            = validator.validate();

        if(validator.isValid()){
            FlexoResourceCenter<?> rc   = projectLoader.getServiceManager().getResourceCenterService().getFlexoResourceCenter(IdUtils.decodeId(validator.getRcId()));
            CreateProject action        = CreateProject.actionType.makeNewAction((RepositoryFolder) rc.getRootFolder(), null, projectLoader.getServiceManager().getDefaultEditor());

            action.setNewProjectName(validator.getName());
            action.doAction();

            FlexoProject<?> project = action.getNewProject();

            projectLoader.getRootProjects().add(project);

            context.response().end(JsonSerializer.projectSerializer(project).encodePrettily());
        } else {
            badValidation(context, errors);
        }
    }

    /**
     * It gets the project id from the request, decodes it, finds the project in the project loader, and if it exists,
     * serializes it and sends it back to the client
     *
     * @param context the routing context
     */
    public void get(RoutingContext context) {
        String id               = context.request().getParam("id").trim();
        FlexoProject<?> project = ProjectsRepository.getProjectById(virtualModelLibrary, id);

        if (project != null){
            context.response().end(JsonSerializer.projectSerializer(project).encodePrettily());
        } else {
            notFound(context);
        }

    }

    public void edit(RoutingContext context) {}

    /**
     * It deletes a project
     *
     * @param context the routing context
     */
    public void delete(RoutingContext context) {
        String id               = context.request().getParam("id").trim();
        FlexoProject<?> project = ProjectsRepository.getProjectById(virtualModelLibrary, id);

        if (project != null){
            virtualModelLibrary.getServiceManager().getProjectLoaderService().removeFromRootProjects(project);
            project.delete();

            emptyResponse(context);
        } else {
            notFound(context);
        }
    }

    /**
     * It gets the project id from the request, gets the project from the repository, creates a new JsonArray, iterates
     * over the project's root folder's children, and adds each child to the JsonArray
     *
     * @param context the context of the request
     */
    public void folders(RoutingContext context) {
        String id               = context.request().getParam("id").trim();
        FlexoProject<?> project = ProjectsRepository.getProjectById(virtualModelLibrary, id);
        JsonArray result        = new JsonArray();

        for (RepositoryFolder folder: project.getRootFolder().getChildren()) {

            result.add(JsonSerializer.repositoryFolderSerializer(folder));
        }
        context.response().end(result.encodePrettily());
    }

    /**
     * It adds a folder to a project
     *
     * @param context the routing context
     */
    public void addFolder(RoutingContext context) {
        String id                   = context.request().getParam("id").trim();
        FlexoProject<?> project     = ProjectsRepository.getProjectById(virtualModelLibrary, id);
        FolderValidator validator   = new FolderValidator(context.request());
        JsonArray errors            = validator.validate();

        if(validator.isValid()){
            RepositoryFolder parent = Helpers.getFolderFromPath(validator.getParent(), project);

            if (parent == null) {
                parent = project.getRootFolder();
            }

            AddRepositoryFolder addRepositoryFolder = AddRepositoryFolder.actionType.makeNewAction(parent, null, editor);
            addRepositoryFolder.setNewFolderName(validator.getName());
            addRepositoryFolder.doAction();

            context.response().end(JsonSerializer.repositoryFolderSerializer(addRepositoryFolder.getNewFolder()).encodePrettily());
        } else {
            badValidation(context, errors);
        }
    }

    /**
     *  It takes a file upload, unzips it, and loads the project contained in it
     *
     * @param context the routing context
     */
    public void upload(RoutingContext context) {
        ProjectsValidator validator = new ProjectsValidator(projectLoader, context.request());
        JsonArray errors            = validator.validateUpload(context.fileUploads());

        if(validator.isValid()){
            List<FileUpload> fileUploadSet          = context.fileUploads();
            Iterator<FileUpload> fileUploadIterator = fileUploadSet.iterator();
            JsonArray results 					    = new JsonArray();

            while (fileUploadIterator.hasNext()){
                FileUpload fileUpload 	                = fileUploadIterator.next();
                Buffer uploadedFile 	                = context.vertx().fileSystem().readFileBlocking(fileUpload.uploadedFileName());
                FlexoResourceCenter<?> resourceCenter   = projectLoader.getServiceManager().getResourceCenterService().getFlexoResourceCenter(IdUtils.decodeId(validator.getRcId()));
                
                try {
                    String targetDir    = resourceCenter.getRootFolder().getFullQualifiedPath();
                    File[] files        = Helpers.unzipFile(targetDir, uploadedFile);

                    for (File containedResource: files) {
                        if (containedResource.getName().endsWith(".prj")){
                            String projectPath = targetDir + "/" + containedResource.getName();

                            try {
                                FlexoEditor project = projectLoader.loadProject(new File(projectPath));
                                results.add(JsonSerializer.projectSerializer(project.getProject()));
                            } catch (ProjectLoadingCancelledException | ProjectInitializerException e) {
                                Log.error(e.getMessage());
                            }
                        }
                    }

                } catch (IOException e) {
                    badRequest(context);
                }
            }
            context.response().end(results.encodePrettily());
        } else {
            badValidation(context, errors);
        }
    }

    /**
     * It gets the project id from the request, gets the project from the repository, and then returns a JSON array of all
     * the resources in the project
     *
     * @param context the context of the request
     */
    public void resources(RoutingContext context) {
        String id               = context.request().getParam("id").trim();
        FlexoProject<?> project = ProjectsRepository.getProjectById(virtualModelLibrary, id);

        if (project != null){
            JsonArray result = new JsonArray();

            Collection<FlexoResource<?>> resources  = project.getAllResources();
            HashSet<String> names                   = new HashSet<>();

            resources.removeIf(e -> !names.add(IdUtils.encodeuri(e.getName())));

            for (FlexoResource<?> resource : resources) {
                result.add(JsonUtils.getResourceDescription(resource, technologyAdapterRestService));
            }
            context.response().end(result.encodePrettily());

        } else {
            notFound(context);
        }
    }

    /**
     * It loads all the resources of a project
     *
     * @param context the routing context
     */
    public void loadResources(RoutingContext context){
        String id               = context.request().getParam("id").trim();
        FlexoProject<?> project = ProjectsRepository.getProjectById(virtualModelLibrary, id);
        JsonArray loaded        = new JsonArray();
        JsonArray failed        = new JsonArray();
        JsonObject results      = new JsonObject();

        if (project != null){
            for (FlexoResource<?> resource : project.getAllResources()) {
                try {
                    resource.loadResourceData();
                    loaded.add(JsonSerializer.getResourceDescription(resource, technologyAdapterRestService));
                } catch (ResourceLoadingCancelledException | FileNotFoundException | FlexoException e) {
                    failed.add(JsonSerializer.getResourceDescription(resource, technologyAdapterRestService));
                }
            }

            results.put("Loaded", loaded);
            results.put("Failed", failed);
            context.response().end(results.encodePrettily());
        } else {
            notFound(context);
        }
    }

    /**
     * It takes a project id, finds the project, then iterates through the project's root folder and adds each subfolder to
     * the project
     *
     * @param context the routing context
     */
    public void loadFolders(RoutingContext context){
        String id               = context.request().getParam("id").trim();
        FlexoProject<?> project = ProjectsRepository.getProjectById(virtualModelLibrary, id);
        JsonArray results       = new JsonArray();

        if (project != null){
            Path startDir = Paths.get(project.getRootFolder().getFullQualifiedPath());
            try {
                DirectoryStream<Path> stream = Files.newDirectoryStream(startDir);

                for (Path path : stream) {
                    if (Files.isDirectory(path)) {
                        RepositoryFolder parent = project.getRootFolder();

                        AddRepositoryFolder addRepositoryFolder = AddRepositoryFolder.actionType.makeNewAction(parent, null, editor);

                        addRepositoryFolder.setNewFolderName(path.getFileName().toString());
                        addRepositoryFolder.doAction();

                        results.add(JsonSerializer.repositoryFolderSerializer(addRepositoryFolder.getNewFolder()));
                    }
                }
            } catch (Exception e) {
                badRequest(context);
            }

            context.response().end(results.encodePrettily());
        } else {
            notFound(context);
        }
    }
}