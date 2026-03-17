/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.channel.internal.jaxrs.exception.mapper;

import com.liferay.portal.kernel.exception.NoSuchModelException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.BaseExceptionMapper;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.Problem;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

import org.osgi.service.component.annotations.Component;

/**
 * @author Magdalena Jedraszak
 */
@Component(
	property = {
		"osgi.jaxrs.application.select=(osgi.jaxrs.name=Liferay.Headless.Commerce.Admin.Channel)",
		"osgi.jaxrs.extension=true",
		"osgi.jaxrs.name=Liferay.Headless.Commerce.Admin.Channel.NoSuchPageTemplateExceptionMapper",
		"service.ranking:Integer=1000"
	},
	service = ExceptionMapper.class
)
public class NoSuchPageTemplateExceptionMapper
	extends BaseExceptionMapper<PortalException> {

	@Override
	public Response toResponse(PortalException portalException) {
		ExceptionMapper<NoSuchModelException> exceptionMapper =
			_providers.getExceptionMapper(NoSuchModelException.class);

		if (exceptionMapper != null) {
			return exceptionMapper.toResponse(
				new NoSuchModelException(portalException));
		}

		return super.toResponse(portalException);
	}

	@Override
	protected Problem getProblem(PortalException portalException) {
		return new Problem(Response.Status.NOT_FOUND, "Not Found");
	}

	@Context
	private Providers _providers;

}