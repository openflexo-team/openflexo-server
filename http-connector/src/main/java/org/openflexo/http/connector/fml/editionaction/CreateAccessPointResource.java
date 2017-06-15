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

package org.openflexo.http.connector.fml.editionaction;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.InvalidArgumentException;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.editionaction.AbstractCreateResource;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.http.connector.HttpModelSlot;
import org.openflexo.http.connector.HttpTechnologyAdapter;
import org.openflexo.http.connector.fml.AccessPointType;
import org.openflexo.http.connector.fml.editionaction.CreateAccessPointResource.CreateAccessPointResourceImpl;
import org.openflexo.http.connector.model.AccessPoint;
import org.openflexo.http.connector.rm.AccessPointResource;
import org.openflexo.http.connector.rm.AccessPointResourceFactory;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.exceptions.ModelDefinitionException;

/**
 * {@link EditionAction} used to create an empty AccessPoint resource
 * 
 * @author charlie, sylvain
 *
 */
@ModelEntity
@ImplementationClass(CreateAccessPointResourceImpl.class)
@XMLElement
@FML("CreateAccessPointResource")
public interface CreateAccessPointResource extends AbstractCreateResource<HttpModelSlot, AccessPoint, HttpTechnologyAdapter> {

	@PropertyIdentifier(type = String.class)
	String URL_KEY = "url";
	@PropertyIdentifier(type = String.class)
	String USER_KEY = "user";
	@PropertyIdentifier(type = String.class)
	String PASSWORD_KEY = "password";

	@Getter(URL_KEY)
	@XMLAttribute
	DataBinding<String> getUrl();

	@Setter(URL_KEY)
	void setUrl(DataBinding<String> url);

	@Getter(USER_KEY)
	@XMLAttribute
	DataBinding<String> getUser();

	@Setter(USER_KEY)
	void setUser(DataBinding<String> user);

	@Getter(PASSWORD_KEY)
	@XMLAttribute
	DataBinding<String> getPassword();

	@Setter(PASSWORD_KEY)
	void setPassword(DataBinding<String> password);

	abstract class CreateAccessPointResourceImpl extends AbstractCreateResourceImpl<HttpModelSlot, AccessPoint, HttpTechnologyAdapter>
			implements CreateAccessPointResource {

		private DataBinding<String> url;
		private DataBinding<String> user;
		private DataBinding<String> password;

		@Override
		public Type getAssignableType() {
			FlexoProperty<AccessPoint> flexoProperty = getAssignedFlexoProperty();
			if (flexoProperty instanceof HttpModelSlot) {
				HttpModelSlot httpModelSlot = (HttpModelSlot) flexoProperty;
				if (httpModelSlot != null && httpModelSlot.getAccessedVirtualModel() != null) {
					return new AccessPointType((VirtualModelInstanceType) httpModelSlot.getAccessedVirtualModel().getInstanceType());
				}
			}
			return AccessPoint.class;
		}

		@Override
		public AccessPoint execute(RunTimeEvaluationContext evaluationContext) throws FlexoException {
			try {
				String resourceName = getResourceName(evaluationContext);
				String resourceURI = getResourceURI(evaluationContext);
				FlexoResourceCenter<?> rc = getResourceCenter(evaluationContext);

				AccessPointResource newResource = createResource(
						getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(HttpTechnologyAdapter.class),
						AccessPointResourceFactory.class, rc, resourceName, resourceURI, getRelativePath(), ".url", true);
				AccessPoint data = newResource.getResourceData(null);

				FlexoProperty<AccessPoint> flexoProperty = getAssignedFlexoProperty();
				if (flexoProperty instanceof HttpModelSlot) {
					HttpModelSlot httpModelSlot = (HttpModelSlot) flexoProperty;
					String url = null;
					try {
						if (getUrl().isValid()) {
							url = getUrl().getBindingValue(evaluationContext);
						}
						else if (httpModelSlot.getUrl().isValid()) {
							url = httpModelSlot.getUrl().getBindingValue(evaluationContext);
						}
					} catch (TypeMismatchException | NullReferenceException | InvocationTargetException e) {
						e.printStackTrace();
					}
					String user = null;
					try {
						if (getUser().isValid()) {
							user = getUser().getBindingValue(evaluationContext);
						}
						else if (httpModelSlot.getUser().isValid()) {
							user = httpModelSlot.getUser().getBindingValue(evaluationContext);
						}
					} catch (TypeMismatchException | NullReferenceException | InvocationTargetException e) {
						e.printStackTrace();
					}
					String password = null;
					try {
						if (getPassword().isValid()) {
							password = getPassword().getBindingValue(evaluationContext);
						}
						else if (httpModelSlot.getPassword().isValid()) {
							password = httpModelSlot.getPassword().getBindingValue(evaluationContext);
						}
					} catch (TypeMismatchException | NullReferenceException | InvocationTargetException e) {
						e.printStackTrace();
					}

					data.setUrl(url);
					data.setUser(user);
					data.setPassword(password);
					data.setModelSlot(httpModelSlot);
					data.setOwnerInstance(evaluationContext.getVirtualModelInstance());
					newResource.getFactory().initializeModel(data, httpModelSlot.getCreationScheme(), httpModelSlot.getParameters(),
							evaluationContext);
				}
				else {
					throw new InvalidArgumentException("AccessPoint creation must be affected to a HTTPModelSlot");
				}

				return data;
			} catch (ModelDefinitionException | FileNotFoundException | ResourceLoadingCancelledException e) {
				throw new FlexoException(e);
			}

		}

		@Override
		public DataBinding<String> getUrl() {
			if (url == null) {
				url = new DataBinding<String>(this, String.class, DataBinding.BindingDefinitionType.GET);
				url.setBindingName("url");
			}
			return url;
		}

		@Override
		public void setUrl(DataBinding<String> url) {
			if (url != null) {
				url.setOwner(this);
				url.setDeclaredType(String.class);
				url.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				url.setBindingName("url");
			}
			this.url = url;
		}

		@Override
		public DataBinding<String> getUser() {
			if (user == null) {
				user = new DataBinding<String>(this, String.class, DataBinding.BindingDefinitionType.GET);
				user.setBindingName("user");
			}
			return user;
		}

		@Override
		public void setUser(DataBinding<String> user) {
			if (user != null) {
				user.setOwner(this);
				user.setDeclaredType(String.class);
				user.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				user.setBindingName("user");
			}
			this.user = user;
		}

		@Override
		public DataBinding<String> getPassword() {
			if (password == null) {
				password = new DataBinding<String>(this, String.class, DataBinding.BindingDefinitionType.GET);
				password.setBindingName("password");
			}
			return password;
		}

		@Override
		public void setPassword(DataBinding<String> password) {
			if (password != null) {
				password.setOwner(this);
				password.setDeclaredType(String.class);
				password.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				password.setBindingName("password");
			}
			this.password = password;
		}

	}
}
