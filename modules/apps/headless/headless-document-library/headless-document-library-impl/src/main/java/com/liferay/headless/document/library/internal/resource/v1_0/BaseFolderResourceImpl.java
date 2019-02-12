/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.headless.document.library.internal.resource.v1_0;

import com.liferay.headless.document.library.dto.v1_0.Folder;
import com.liferay.headless.document.library.resource.v1_0.FolderResource;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.TransformUtil;

import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public abstract class BaseFolderResourceImpl implements FolderResource {

	public static final String ODATA_ENTITY_MODEL_NAME =
		"com_liferay_headless_document_library_dto_v1_0_FolderEntityModel";

	@Override
	public Response deleteFolders(Long foldersId) throws Exception {
		Response.ResponseBuilder responseBuilder = Response.ok();

		return responseBuilder.build();
	}

	@Override
	public Folder getDocumentsRepositories(Long documentsRepositoriesId)
		throws Exception {

		return new Folder();
	}

	@Override
	public Page<Folder> getDocumentsRepositoriesFoldersPage(
			Long documentsRepositoriesId, Pagination pagination)
		throws Exception {

		return Page.of(Collections.emptyList());
	}

	@Override
	public Folder getFolder(Long foldersId) throws Exception {
		return new Folder();
	}

	@Override
	public Page<Folder> getFoldersFoldersPage(
			Long foldersId, Pagination pagination)
		throws Exception {

		return Page.of(Collections.emptyList());
	}

	@Override
	public Folder postDocumentsRepositoriesFolder(
			Long documentsRepositoriesId, Folder folderParameter)
		throws Exception {

		return new Folder();
	}

	@Override
	public Folder postDocumentsRepositoriesFoldersBatchCreate(
			Long documentsRepositoriesId, Folder folderParameter)
		throws Exception {

		return new Folder();
	}

	@Override
	public Folder postFoldersFolder(Long foldersId, Folder folderParameter)
		throws Exception {

		return new Folder();
	}

	@Override
	public Folder postFoldersFoldersBatchCreate(
			Long foldersId, Folder folderParameter)
		throws Exception {

		return new Folder();
	}

	@Override
	public Folder putFolder(Long foldersId, Folder folderParameter)
		throws Exception {

		return new Folder();
	}

	protected Response buildNoContentResponse() {
		Response.ResponseBuilder responseBuilder = Response.noContent();

		return responseBuilder.build();
	}

	protected <T, R> List<R> transform(
		List<T> list, UnsafeFunction<T, R, Exception> unsafeFunction) {

		return TransformUtil.transform(list, unsafeFunction);
	}

	@Context
	protected AcceptLanguage acceptLanguage;

	@Context
	protected Company company;

}