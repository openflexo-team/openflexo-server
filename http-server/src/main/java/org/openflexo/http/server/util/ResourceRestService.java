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

import java.util.Collection;
import java.util.List;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.*;
import org.openflexo.foundation.fml.rt.*;
import org.openflexo.foundation.fml.rt.action.ActionSchemeAction;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionFactory;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceImpl;
import org.openflexo.http.server.OpenFlexoServer;
import org.openflexo.http.server.RouteService;
import org.openflexo.http.server.json.JsonError;
import org.openflexo.http.server.json.JsonSerializer;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Creates REST entry point to serve resources associated with data.
 */
public abstract class ResourceRestService<D, R> {

	public static final String DETAILED_PARAM = "detailed";

	protected final String prefix;

	protected final Class<R> resourceClass;

	protected final JsonSerializer serializer;

	public ResourceRestService(String prefix, Class<R> resourceClass, JsonSerializer serializer) {
		this.prefix = prefix;
		this.resourceClass = resourceClass;
		this.serializer = serializer;
	}

	public String getPrefix() {
		return prefix;
	}

	public Class<R> getResourceClass() {
		return resourceClass;
	}

	protected abstract Collection<R> allResources();

	protected abstract R findResource(String id);

	protected abstract D loadResource(R resource) throws Exception;

	protected abstract Collection<Object> allObjects(R resource);

	protected abstract Object findObject(R resource, String id);

	public void addRoutes(Router router) {
		router.get(prefix).produces(RouteService.JSON).handler(this::serveResourceList);
		router.get(prefix + "/:id").produces(RouteService.JSON).handler(this::serveRoot);
		router.get(prefix + "/:id/object").produces(RouteService.JSON).handler(this::serveEntityList);
		router.get(prefix + "/:id/object/:eid").produces(RouteService.JSON).handler(this::serveEntity);
		router.get(prefix + "/:id/describe").produces(RouteService.JSON).handler(this::describeEntity);
		router.post(prefix + "/:id/createInstance").produces(RouteService.JSON).handler(this::createInstance);
		router.post(prefix + "/:id/executeBehaviour").produces(RouteService.JSON).handler(this::executeBehaviour);
	}

	private void executeBehaviour(RoutingContext context) {
		try {
			// Getting the context
			String id = context.request().getParam(("id"));
			String behaviourName = context.request().getParam(("behaviourName"));
			R resource = getLoadedResource(id);
			FMLRTVirtualModelInstanceResourceImpl rtvmiri = (FMLRTVirtualModelInstanceResourceImpl) resource;
			FMLRTVirtualModelInstance rtvmi = rtvmiri.getResourceData();
			VirtualModel vm = rtvmi.getVirtualModel();

			// Find the behaviour
			List<FlexoBehaviour> lBehaviours = vm.getAccessibleFlexoBehaviours(false);
			FlexoBehaviour selectedBehavour = vm.getAccessibleFlexoBehaviours(false).stream()
					.filter((fb) -> fb.getName().equals(behaviourName)).findFirst().get();

			// Only ActionScheme supported
			if (selectedBehavour instanceof ActionScheme) {
				ActionScheme as = (ActionScheme) selectedBehavour;
				ActionSchemeActionFactory actionType = new ActionSchemeActionFactory(as, rtvmi.getFlexoConceptInstance());
				ActionSchemeAction action = actionType.makeNewAction(rtvmi.getVirtualModelInstance(), null, rtvmi.getEditor());

				for (FlexoBehaviourParameter p : as.getParameters()) {
					// Only string parameters supported
					String paramType = p.getType().getTypeName();
					if (paramType.equals("java.lang.String")) {
						String parameterValue = context.request().getParam((p.getName()));
						if (parameterValue == null) {
							throw new Exception("Missing parameter value : " + p.getName());
						}
						action.setParameterValue(p, parameterValue);
					}
					else {
						throw new Exception("Only string parameters are supported");
					}
				}
				action.doAction();
			}
			else {
				throw new Exception("On");
			}

			// Save the resource
			rtvmiri.save();

		} catch (Exception e) {
			error(context, e);
		}

		// Response
		JsonObject successResult = new JsonObject();
		successResult.put("Status", "Ok");
		context.response().end(successResult.encodePrettily());
	}

	private void createInstance(RoutingContext context) {
		try {
			// Getting the context
			String id = context.request().getParam(("id"));
			String instanceName = context.request().getParam(("instanceName"));
			String instanceDescription = context.request().getParam(("instanceDescription"));
			String creationSchemeName = context.request().getParam(("creationSchemeName"));
			R resource = getLoadedResource(id);

			if (resource != null) {
				boolean detailed = context.request().getParam(DETAILED_PARAM) != null;

				if (resource instanceof CompilationUnitResource) {

					CompilationUnitResource vmr = (CompilationUnitResource) resource;
					VirtualModel vm = vmr.getCompilationUnit().getVirtualModel();

					// Check for creation scheme
					if (!vm.hasCreationScheme()) {
						throw new Exception("The model doesn't have any creation scheme");
					}

					// Find the requested creation behaviour
					CreationScheme selectedCreationScheme = vm.getCreationSchemes().stream()
							.filter((cs) -> cs.getName().equals(creationSchemeName)).findFirst().get();

					// Create the instantiation action
					FlexoEditor editor = OpenFlexoServer.Options.globalEditor;
					CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType
							.makeNewAction(editor.getProject().getVirtualModelInstanceRepository().getRootFolder(), null, editor);
					action.setCreationScheme(selectedCreationScheme);
					action.setNewVirtualModelInstanceName(instanceName);
					action.setNewVirtualModelInstanceTitle(instanceDescription);
					action.setVirtualModel(vm);

					// Fill parameters
					for (FlexoBehaviourParameter p : selectedCreationScheme.getParameters()) {
						// Only string parameters supported
						String paramType = p.getType().getTypeName();
						if (paramType.equals("java.lang.String")) {
							String parameterValue = context.request().getParam((p.getName()));
							if (parameterValue == null) {
								throw new Exception("Missing parameter value : " + p.getName());
							}
							action.setParameterValue(p, parameterValue);
						}
						else {
							throw new Exception("Only string parameters are supported");
						}
					}

					action.doAction();

					// Check for success
					if (action.hasActionExecutionSucceeded()) {
						FMLRTVirtualModelInstance newVirtualModelInstance = action.getNewVirtualModelInstance();
					}
					else {
						throw new Exception("Error while creation new virtual model");
					}

					// Response
					JsonObject successResult = new JsonObject();
					successResult.put("Status", "Ok");
					context.response().end(successResult.encodePrettily());
				}
				else {
					throw new Exception("The resource is not a VirtualModel, cannot instantiate");
				}
			}
			else {
				notFound(context);
			}
		} catch (Exception e) {
			error(context, e);
		}
	}

	private void serveResourceList(RoutingContext context) {
		try {
			boolean detailed = context.request().getParam(DETAILED_PARAM) != null;
			JsonArray result = new JsonArray();
			for (R served : allResources()) {
				result.add(serializer.toJson(served, detailed));
			}
			context.response().end(result.encodePrettily());

		} catch (Exception e) {
			error(context, e);
		}
	}

	/**
	 * Retrieves and loads (if not already loaded) resource with given id then returns the resource.
	 *
	 * @param id
	 *            resource id
	 * @return the loaded resource
	 * @throws Exception
	 *             if resource can't be loaded
	 */
	protected R getLoadedResource(String id) throws Exception {
		// Unused String uri = IdUtils.decodeId(id);
		R served = findResource(id);
		if (served != null) {
			// load data if needed
			loadResource(served);
		}
		return served;
	}

	private void serveRoot(RoutingContext context) {
		try {
			String id = context.request().getParam(("id"));
			R resource = getLoadedResource(id);
			if (resource != null) {
				boolean detailed = context.request().getParam(DETAILED_PARAM) != null;
				JsonObject vpJson = (JsonObject) serializer.toJson(resource, detailed);
				context.response().end(vpJson.encodePrettily());
			}
			else {
				notFound(context);
			}
		} catch (Exception e) {
			error(context, e);
		}
	}

	private void serveEntityList(RoutingContext context) {
		try {
			String id = context.request().getParam(("id"));
			R resource = getLoadedResource(id);
			if (resource != null) {
				boolean detailed = context.request().getParam(DETAILED_PARAM) != null;
				Collection<Object> embeddedObjects = allObjects(resource);
				JsonArray result = new JsonArray();
				for (Object object : embeddedObjects) {
					result.add(serializer.toJson(object, detailed));
				}

				context.response().end(result.encodePrettily());

			}
			else {
				notFound(context);
			}
		} catch (NumberFormatException e) {
			notFound(context);
		} catch (Exception e) {
			error(context, e);
		}
	}

	private void serveEntity(RoutingContext context) {
		try {
			String id = context.request().getParam(("id"));
			String eid = context.request().getParam(("eid"));
			R resource = getLoadedResource(id);
			if (resource != null) {
				Object object = findObject(resource, eid);
				if (object != null) {
					boolean detailed = context.request().getParam(DETAILED_PARAM) != null;
					JsonObject vpJson = (JsonObject) serializer.toJson(object, detailed);
					context.response().end(vpJson.encodePrettily());
				}
				else {
					notFound(context);
				}
			}
			else {
				notFound(context);
			}
		} catch (NumberFormatException e) {
			notFound(context);
		} catch (Exception e) {
			error(context, e);
		}
	}

	private static void notFound(RoutingContext context) {
		context.response().setStatusCode(404).close();
	}

	private static void error(RoutingContext context, Throwable e) {
		HttpServerResponse response = context.response();
		response.end(new JsonError(e).toString());
	}

	private JsonArray convertEmbeddedFlexoInstancesListToJson(FlexoConceptInstance rootFci) {
		// Print root flexo concept instances
		JsonArray fciEmbedded = new JsonArray();
		for(FlexoConceptInstance fci : rootFci.getEmbeddedFlexoConceptInstances()) {
			JsonObject fciObject = new JsonObject();
			fciObject.put("id", fci.getFlexoID());
			fciObject.put("type", fci.getFlexoConcept().getName());
			fciObject.put("actors", convertFciActorsToJson(fci));
			fciEmbedded.add(fciObject);
		}
		return fciEmbedded;

	}

	private JsonArray convertFciActorsToJson(FlexoConceptInstance fci) {
		JsonArray result = new JsonArray();
		for(ActorReference actor : fci.getActors()) {
			JsonObject actorObj = new JsonObject();
			PropertyCardinality actorCardinality = actor.getFlexoRole().getCardinality();
			if(actorCardinality == PropertyCardinality.One || actorCardinality == PropertyCardinality.ZeroOne ) {
				actorObj.put("id", actor.getFlexoID());
				actorObj.put("name", actor.getRoleName());

				if(actor instanceof PrimitiveActorReference){
					actorObj.put("value", actor.getModellingElement().toString());
				}
				if(actor instanceof ModelObjectActorReference){
					actorObj.put("reference", convertFciActorsToJson((FlexoConceptInstance) actor.getModellingElement()).getJsonObject(0));
				}
				result.add(actorObj);
			} else {

			}
		}
		return result;
	}

	private void describeEntity(RoutingContext context) {
		try {
			// Getting the context
			String id = context.request().getParam(("id"));
			R resource = getLoadedResource(id);
			FMLRTVirtualModelInstanceResourceImpl rtvmiri = (FMLRTVirtualModelInstanceResourceImpl) resource;
			FMLRTVirtualModelInstance  rtvmi = rtvmiri.getVirtualModelInstance();


			JsonObject result = new JsonObject();
			result.put("id", rtvmi.getFlexoID());
			result.put("name", rtvmi.getTitle());
			result.put("type", rtvmi.getVirtualModel().getName());

			// Print basic actors
			result.put("actors", convertFciActorsToJson(rtvmi));

			// Print root flexo concept instances
			JsonArray fciEmbedded = new JsonArray();
			for(FlexoConceptInstance fci : rtvmi.getAllRootFlexoConceptInstances()) {
				JsonObject fciObject = new JsonObject();
				fciObject.put("id", fci.getFlexoID());
				fciObject.put("type", fci.getFlexoConcept().getName());
				fciObject.put("actors", convertFciActorsToJson(fci));
				fciObject.put("embeddedFlexoConceptInstances", convertEmbeddedFlexoInstancesListToJson(fci));
				fciEmbedded.add(fciObject);
			}
			result.put("embeddedFlexoConceptInstances", fciEmbedded);
			context.response().end(result.encode());
		} catch (Exception e) {
			error(context, e);
		}
	}
}
