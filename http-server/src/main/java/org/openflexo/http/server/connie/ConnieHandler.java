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

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import jline.internal.Log;
import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.BindingPathChangeListener;
import org.openflexo.connie.exception.NotSettableContextException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.http.server.core.TechnologyAdapterRouteService;
import org.openflexo.http.server.util.IdUtils;
import org.openflexo.http.server.util.ResourceUtils;

import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.DecodeException;

//import org.openflexo.toolbox.JsonUtils;
/**
 * WebSocket service to evaluate Connie expressions
 */
public class ConnieHandler implements Handler<ServerWebSocket> {

	private final FlexoServiceManager serviceManager;
	private final TechnologyAdapterRouteService taRouteService;

	private final Map<BindingId, DataBinding<Object>> compiledBindings = new WeakHashMap<>();

	private final Set<ClientConnection> clients = new HashSet<>();

	public ConnieHandler(FlexoServiceManager serviceManager, TechnologyAdapterRouteService taRouteService) {
		this.serviceManager = serviceManager;
		this.taRouteService = taRouteService;
	}

	private DataBinding<Object> getOrCreateBinding(final BindingId id) {
		return compiledBindings.computeIfAbsent(id, (bId) -> {
			Bindable model = getOrCreateBindable(id);
			DataBinding<Object> binding = new DataBinding<>(id.expression, model, Object.class, DataBinding.BindingDefinitionType.GET);
//			binding.decode();
			return binding;
		});
	}

	/**
	 * Finds an object with the given url.
	 *
	 * <b>Work in progress</b> What to do this the prefix ?
	 *
	 * @param url
	 *            object url
	 * @param type
	 *            expected type
	 * @param <T>
	 *            expected type
	 * @return the corresponding object of given type, null other wise.
	 */
	private <T> T findObject(String url, Class<T> type) {
		if (url == null)
			return null;
		String objectInfix = "/object/";
		int indexOfInfix = url.indexOf(objectInfix);
		if (indexOfInfix >= 0) {
			String resourceId = url.substring(0, indexOfInfix);
			int indexOfFirstSlash = resourceId.lastIndexOf('/');
			resourceId = resourceId.substring(indexOfFirstSlash + 1);
			String objectUrl = url.substring(indexOfInfix + objectInfix.length());

			String resourceUri = IdUtils.decodeId(resourceId);
			FlexoResource<?> resource = serviceManager.getResourceManager().getResource(resourceUri);
			ResourceUtils.safeGetResourceOrNull(resource);
			if (resource != null) {
				FlexoObject object = (FlexoObject) resource.findObject(objectUrl, null, null);
				if (type.isInstance(object)) {
					return type.cast(object);
				}
			}
		}
		return null;
	}

	private Bindable getOrCreateBindable(BindingId id) {
		Bindable bindable = findObject(id.contextUrl, Bindable.class);

		if (id.extensions != null && id.extensions.size() > 0) {
			Map<String, Object> objects = new HashMap<>();
			for (Map.Entry<String, String> entry : id.extensions.entrySet()) {
				objects.put(entry.getKey(), findObject(entry.getValue(), Object.class));
			}
			bindable = new ExtendedBindable(bindable, objects);
		}

		return bindable;
	}

	@Override
	public void handle(ServerWebSocket socket) {
		register(new ClientConnection(socket));
	}

	private void register(ClientConnection client) {
		clients.add(client);
	}

	private void unregister(ClientConnection client) {
		clients.remove(client);
	}

	private class ClientConnection {

		private final ServerWebSocket socket;

		private final Map<RuntimeBindingId, BindingPathChangeListener> listenedBindings = new HashMap<>();

		private final Map<RuntimeBindingId, BindingEvaluationContext> contexts = new WeakHashMap<>();

		public ClientConnection(ServerWebSocket socket) {
			this.socket = socket;

			socket.frameHandler(this::handleFrame);
			socket.exceptionHandler(this::exceptionHandler);
			socket.endHandler(this::endHandler);
		}

		private BindingEvaluationContext getOrCreateContext(final RuntimeBindingId id) {
			return contexts.computeIfAbsent(id, (bId) -> {
				BindingEvaluationContext context = findObject(id.runtimeUrl, BindingEvaluationContext.class);

				if (id.extensions != null && id.extensions.size() > 0) {
					Map<String, Object> objects = new HashMap<>();
					for (Map.Entry<String, String> entry : id.extensions.entrySet()) {
						objects.put(entry.getKey(), findObject(entry.getValue(), Object.class));
					}
					context = new ExtendedBindingEvaluationContext(context, objects);
				}

				return context;
			});
		}

		private void handleFrame(WebSocketFrame frame) {
			try {
				ConnieMessage message = ConnieMessage.read(frame.textData());
				if (message instanceof EvaluationRequest) {
					respondToEvaluationRequest((EvaluationRequest) message);
				}
				if (message instanceof ListeningRequest) {
					respondToListeningRequest((ListeningRequest) message);
				}
				if (message instanceof AssignationRequest) {
					respondToAssignationRequest((AssignationRequest) message);
				}
				if (message instanceof SynchronisationRequest) {
					for (ClientConnection client: clients){
						if (!client.equals(this)){
							client.respondToSynchronisationRequest((SynchronisationRequest) message);
						}
					}
				}

			} catch (DecodeException e) {
				String message = "Can't decode request: " + e.getMessage();
				System.out.println(message);
				socket.write(Response.error(message).toBuffer());
			}
		}

		private void respondToSynchronisationRequest(SynchronisationRequest request) {
			String message 		= request.toString();
			Response response 	= new Response(request.id, message, null);

			Log.info(message);


//			socket.write(message.getBytes());
//			socket.write(response.toBuffer());

			socket.writeTextMessage(message);
		}

		private void respondToEvaluationRequest(EvaluationRequest request) {
			Response response = new Response(request.id);
			RuntimeBindingId runtimeBinding = request.runtimeBinding;
			DataBinding<Object> binding = getOrCreateBinding(runtimeBinding.binding);
			if (binding.isValid()) {
				BindingEvaluationContext context = getOrCreateContext(runtimeBinding);
				if (context != null) {
					try {
						Object value = binding.getBindingValue(context);
						response.result = toJson(value, request.detailed);
					} catch (TypeMismatchException | InvocationTargetException | NullReferenceException e) {
						String error = "Can't evaluate binding" + request.runtimeBinding + ": " + e;
						System.out.println(error);
						socket.write(Response.error(request.id, error).toBuffer());
					} catch (ReflectiveOperationException e) {
						String error = "Reflective Operation Exception " + request.runtimeBinding + ": " + e;
						System.out.println(error);
						socket.write(Response.error(request.id, error).toBuffer());
					}
				}
				else {
					String error = "Runtime url '" + request.runtimeBinding + "' is invalid";
					System.out.println(error);
					socket.write(Response.error(request.id, error).toBuffer());
				}

			}
			else {
				response.error = "Invalid binding '" + binding.invalidBindingReason() + "'";
				System.out.println(response.error);
			}
			socket.write(response.toBuffer());
		}

		private void respondToListeningRequest(ListeningRequest request) {
			Response response 				= new Response(request.id);
			RuntimeBindingId runtimeBinding = request.runtimeBinding;
			DataBinding<Object> binding 	= getOrCreateBinding(runtimeBinding.binding);

			if (binding.isValid()) {
				BindingEvaluationContext context = getOrCreateContext(runtimeBinding);
				if (context != null) {

					try {
						Object value = binding.getBindingValue(context);
						response.result = toJson(value, request.detailed);
						;
					} catch (TypeMismatchException | NullReferenceException | InvocationTargetException e) {
						String error = "Can't evaluate binding" + request.runtimeBinding + ": " + e;
						System.out.println(error);
						socket.write(Response.error(request.id, error).toBuffer());
					} catch (ReflectiveOperationException e) {
						throw new RuntimeException(e);
					}

					BindingPathChangeListener<?> listener = listenedBindings.get(runtimeBinding);
					if (listener == null) {
						listener = new BindingPathChangeListener<Object>(binding, context) {
							@Override
							public void bindingValueChanged(Object source, Object newValue) {
								sendChangeEvent(runtimeBinding, newValue);
							}
						};
						listener.startObserving();
					}
					listenedBindings.put(runtimeBinding, listener);

				}
				else {
					String error = "Runtime url '" + request.runtimeBinding + "' is invalid";
					System.out.println(error);
					socket.write(Response.error(request.id, error).toBuffer());
				}

			}
			else {
				response.error = "Invalid binding '" + binding.invalidBindingReason() + "'";
			}
			socket.write(response.toBuffer());
		}

		private void respondToAssignationRequest(AssignationRequest request) {
			Response response = new Response(request.id);

			RuntimeBindingId left = request.left;
			DataBinding<Object> leftBinding = getOrCreateBinding(left.binding);

			if (leftBinding.isValid()) {
				BindingEvaluationContext leftContext = findObject(left.runtimeUrl, BindingEvaluationContext.class);

				if (leftContext != null) {

					Object newValue = null;
					RuntimeBindingId right = request.right;
					if (right != null) {
						// uses binding for new value
						DataBinding<?> valueBinding = getOrCreateBinding(right.binding);
						if (valueBinding.isValid()) {
							BindingEvaluationContext rightContext = findObject(right.runtimeUrl, BindingEvaluationContext.class);
							if (rightContext != null) {
								try {
									Object rightValue = valueBinding.getBindingValue(rightContext);
									leftBinding.setBindingValue(rightValue, leftContext);
									response.result = toJson(rightValue, request.detailed);
								} catch (TypeMismatchException | InvocationTargetException | NullReferenceException e) {
									String error = "Can't evaluate  " + request.left.binding.expression + " and/or "
											+ request.right.binding.expression + ": " + e;
									socket.write(Response.error(request.id, error).toBuffer());
								} catch (NotSettableContextException e) {
									String error = "Can't set expression '" + request.left.binding + "'";
									socket.write(Response.error(request.id, error).toBuffer());
								} catch (ReflectiveOperationException e) {
									String error = "Reflective Operation Exception '" + request.left.binding + "'";
									socket.write(Response.error(request.id, error).toBuffer());
								}
							}
							else {
								StringBuilder message = new StringBuilder("Invalid runtime ");
								message.append("'");
								message.append(request.right.runtimeUrl);
								message.append("': ");
								message.append(valueBinding.invalidBindingReason());
								response.error = message.toString();
							}

						}
						else {
							StringBuilder message = new StringBuilder("Invalid binding ");
							message.append("'");
							message.append(request.right.binding.expression);
							message.append("': ");
							message.append(valueBinding.invalidBindingReason());
							response.error = message.toString();
						}
					}
					else {
						// uses string literal for new value
						newValue = request.value;
						// assigns the new value
						try {
							leftBinding.setBindingValue(newValue, leftContext);
							response.result = toJson(newValue, request.detailed);

						} catch (TypeMismatchException | InvocationTargetException | NullReferenceException e) {
							String error = "Can't evaluate  " + request.left.binding.expression + " and/or "
									+ request.right.binding.expression + ": " + e;
							socket.write(Response.error(request.id, error).toBuffer());
						} catch (NotSettableContextException e) {
							String error = "Can't set expression '" + request.left.binding + "'";
							socket.write(Response.error(request.id, error).toBuffer());
						} catch (ReflectiveOperationException e) {
							String error = "Reflective Operation Exception '" + request.left.binding + "'";
							socket.write(Response.error(request.id, error).toBuffer());
						}

					}

				}
				else {
					StringBuilder message = new StringBuilder("Invalid runtime ");
					message.append("'");
					message.append(request.left.runtimeUrl);
					message.append("': ");
					message.append(leftBinding.invalidBindingReason());
					response.error = message.toString();
				}

			}
			else {
				StringBuilder message = new StringBuilder("Invalid binding ");
				message.append("'");
				message.append(request.left.binding.expression);
				message.append("': ");
				message.append(leftBinding.invalidBindingReason());
				response.error = message.toString();
			}

			socket.write(response.toBuffer());
		}

		private void exceptionHandler(Throwable throwable) {
			System.err.println("Exception in websocket " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
		}

		private void endHandler(Void nothing) {
			for (BindingPathChangeListener<?> listener : listenedBindings.values()) {
				listener.stopObserving();
			}
			listenedBindings.clear();

			unregister(this);
		}

		private void sendChangeEvent(RuntimeBindingId runtimeBindingId, Object newValue) {
			socket.write(new ChangeEvent(runtimeBindingId, toJson(newValue, false)).toBuffer());
		}
	}

	private Object toJson(Object object, boolean detailed) {
		return taRouteService.getSerializer().toJson(object, detailed);
	}

}
