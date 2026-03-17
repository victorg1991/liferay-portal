/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.workflow.internal.jaxrs.exception.mapper;

import com.liferay.portal.kernel.exception.NoSuchModelException;
import com.liferay.portal.kernel.workflow.NoSuchWorkflowDefinitionException;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.BaseExceptionMapper;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.Problem;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael Cavalcanti
 */
@Component(
	property = {
		"osgi.jaxrs.application.select=(osgi.jaxrs.name=Liferay.Headless.Admin.Workflow)",
		"osgi.jaxrs.extension=true",
		"osgi.jaxrs.name=Liferay.Headless.Admin.Workflow.NoSuchWorkflowDefinitionExceptionMapper"
	},
	service = ExceptionMapper.class
)
public class NoSuchWorkflowDefinitionExceptionMapper
	extends BaseExceptionMapper<NoSuchWorkflowDefinitionException> {

	@Override
	public Response toResponse(
		NoSuchWorkflowDefinitionException noSuchWorkflowDefinitionException) {

		ExceptionMapper<NoSuchModelException> exceptionMapper =
			_providers.getExceptionMapper(NoSuchModelException.class);

		if (exceptionMapper != null) {
			return exceptionMapper.toResponse(new NoSuchModelException());
		}

		return super.toResponse(noSuchWorkflowDefinitionException);
	}

	@Override
	protected Problem getProblem(
		NoSuchWorkflowDefinitionException noSuchWorkflowDefinitionException) {

		return new Problem(Response.Status.NOT_FOUND, "Not Found");
	}

	@Context
	private Providers _providers;

}