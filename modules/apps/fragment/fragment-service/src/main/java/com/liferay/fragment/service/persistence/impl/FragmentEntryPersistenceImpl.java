/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.service.persistence.impl;

import com.liferay.fragment.exception.DuplicateFragmentEntryExternalReferenceCodeException;
import com.liferay.fragment.exception.NoSuchEntryException;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryTable;
import com.liferay.fragment.model.impl.FragmentEntryImpl;
import com.liferay.fragment.model.impl.FragmentEntryModelImpl;
import com.liferay.fragment.service.persistence.FragmentEntryPersistence;
import com.liferay.fragment.service.persistence.FragmentEntryUtil;
import com.liferay.fragment.service.persistence.impl.constants.FragmentPersistenceConstants;
import com.liferay.portal.kernel.change.tracking.CTColumnResolutionType;
import com.liferay.portal.kernel.configuration.Configuration;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.dao.orm.FinderCache;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.dao.orm.SessionFactory;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.sanitizer.Sanitizer;
import com.liferay.portal.kernel.sanitizer.SanitizerException;
import com.liferay.portal.kernel.sanitizer.SanitizerUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.persistence.change.tracking.helper.CTPersistenceHelper;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.service.persistence.impl.CollectionPersistenceFinder;
import com.liferay.portal.kernel.service.persistence.impl.FinderColumn;
import com.liferay.portal.kernel.service.persistence.impl.UniquePersistenceFinder;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The persistence implementation for the fragment entry service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
@Component(service = FragmentEntryPersistence.class)
public class FragmentEntryPersistenceImpl
	extends BasePersistenceImpl<FragmentEntry, NoSuchEntryException>
	implements FragmentEntryPersistence {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use <code>FragmentEntryUtil</code> to access the fragment entry persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY =
		FragmentEntryImpl.class.getName();

	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List1";

	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List2";

	private CollectionPersistenceFinder<FragmentEntry, NoSuchEntryException>
		_collectionPersistenceFinderByUuid;

	/**
	 * Returns an ordered range of all the fragment entries where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByUuid(
		String uuid, int start, int end,
		OrderByComparator<FragmentEntry> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByUuid.find(
			finderCache, new Object[] {uuid}, start, end, orderByComparator,
			useFinderCache);
	}

	/**
	 * Returns the first fragment entry in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByUuid_First(
			String uuid, OrderByComparator<FragmentEntry> orderByComparator)
		throws NoSuchEntryException {

		return _collectionPersistenceFinderByUuid.findFirst(
			finderCache, new Object[] {uuid}, orderByComparator);
	}

	/**
	 * Returns the first fragment entry in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByUuid_First(
		String uuid, OrderByComparator<FragmentEntry> orderByComparator) {

		return _collectionPersistenceFinderByUuid.fetchFirst(
			finderCache, new Object[] {uuid}, orderByComparator);
	}

	/**
	 * Removes all the fragment entries where uuid = &#63; from the database.
	 *
	 * @param uuid the uuid
	 */
	@Override
	public void removeByUuid(String uuid) {
		_collectionPersistenceFinderByUuid.remove(
			finderCache, new Object[] {uuid});
	}

	/**
	 * Returns the number of fragment entries where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByUuid(String uuid) {
		return _collectionPersistenceFinderByUuid.count(
			finderCache, new Object[] {uuid});
	}

	private UniquePersistenceFinder<FragmentEntry, NoSuchEntryException>
		_uniquePersistenceFinderByUUID_G;

	/**
	 * Returns the fragment entry where uuid = &#63; and groupId = &#63; or throws a <code>NoSuchEntryException</code> if it could not be found.
	 *
	 * @param uuid the uuid
	 * @param groupId the group ID
	 * @return the matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByUUID_G(String uuid, long groupId)
		throws NoSuchEntryException {

		return _uniquePersistenceFinderByUUID_G.find(
			finderCache, new Object[] {uuid, groupId});
	}

	/**
	 * Returns the fragment entry where uuid = &#63; and groupId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param uuid the uuid
	 * @param groupId the group ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByUUID_G(
		String uuid, long groupId, boolean useFinderCache) {

		return _uniquePersistenceFinderByUUID_G.fetch(
			finderCache, new Object[] {uuid, groupId}, useFinderCache);
	}

	/**
	 * Removes the fragment entry where uuid = &#63; and groupId = &#63; from the database.
	 *
	 * @param uuid the uuid
	 * @param groupId the group ID
	 * @return the fragment entry that was removed
	 */
	@Override
	public FragmentEntry removeByUUID_G(String uuid, long groupId)
		throws NoSuchEntryException {

		FragmentEntry fragmentEntry = findByUUID_G(uuid, groupId);

		return remove(fragmentEntry);
	}

	/**
	 * Returns the number of fragment entries where uuid = &#63; and groupId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param groupId the group ID
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByUUID_G(String uuid, long groupId) {
		return _uniquePersistenceFinderByUUID_G.count(
			finderCache, new Object[] {uuid, groupId});
	}

	private CollectionPersistenceFinder<FragmentEntry, NoSuchEntryException>
		_collectionPersistenceFinderByUuid_C;

	/**
	 * Returns an ordered range of all the fragment entries where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByUuid_C(
		String uuid, long companyId, int start, int end,
		OrderByComparator<FragmentEntry> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByUuid_C.find(
			finderCache, new Object[] {uuid, companyId}, start, end,
			orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first fragment entry in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByUuid_C_First(
			String uuid, long companyId,
			OrderByComparator<FragmentEntry> orderByComparator)
		throws NoSuchEntryException {

		return _collectionPersistenceFinderByUuid_C.findFirst(
			finderCache, new Object[] {uuid, companyId}, orderByComparator);
	}

	/**
	 * Returns the first fragment entry in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByUuid_C_First(
		String uuid, long companyId,
		OrderByComparator<FragmentEntry> orderByComparator) {

		return _collectionPersistenceFinderByUuid_C.fetchFirst(
			finderCache, new Object[] {uuid, companyId}, orderByComparator);
	}

	/**
	 * Removes all the fragment entries where uuid = &#63; and companyId = &#63; from the database.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 */
	@Override
	public void removeByUuid_C(String uuid, long companyId) {
		_collectionPersistenceFinderByUuid_C.remove(
			finderCache, new Object[] {uuid, companyId});
	}

	/**
	 * Returns the number of fragment entries where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByUuid_C(String uuid, long companyId) {
		return _collectionPersistenceFinderByUuid_C.count(
			finderCache, new Object[] {uuid, companyId});
	}

	private UniquePersistenceFinder<FragmentEntry, NoSuchEntryException>
		_uniquePersistenceFinderByHeadId;

	/**
	 * Returns the fragment entry where headId = &#63; or throws a <code>NoSuchEntryException</code> if it could not be found.
	 *
	 * @param headId the head ID
	 * @return the matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByHeadId(long headId) throws NoSuchEntryException {
		return _uniquePersistenceFinderByHeadId.find(
			finderCache, new Object[] {headId});
	}

	/**
	 * Returns the fragment entry where headId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param headId the head ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByHeadId(long headId, boolean useFinderCache) {
		return _uniquePersistenceFinderByHeadId.fetch(
			finderCache, new Object[] {headId}, useFinderCache);
	}

	/**
	 * Removes the fragment entry where headId = &#63; from the database.
	 *
	 * @param headId the head ID
	 * @return the fragment entry that was removed
	 */
	@Override
	public FragmentEntry removeByHeadId(long headId)
		throws NoSuchEntryException {

		FragmentEntry fragmentEntry = findByHeadId(headId);

		return remove(fragmentEntry);
	}

	/**
	 * Returns the number of fragment entries where headId = &#63;.
	 *
	 * @param headId the head ID
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByHeadId(long headId) {
		return _uniquePersistenceFinderByHeadId.count(
			finderCache, new Object[] {headId});
	}

	private CollectionPersistenceFinder<FragmentEntry, NoSuchEntryException>
		_collectionPersistenceFinderByFragmentCollectionId;

	/**
	 * Returns an ordered range of all the fragment entries where fragmentCollectionId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param fragmentCollectionId the fragment collection ID
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByFragmentCollectionId(
		long fragmentCollectionId, int start, int end,
		OrderByComparator<FragmentEntry> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByFragmentCollectionId.find(
			finderCache, new Object[] {fragmentCollectionId}, start, end,
			orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first fragment entry in the ordered set where fragmentCollectionId = &#63;.
	 *
	 * @param fragmentCollectionId the fragment collection ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByFragmentCollectionId_First(
			long fragmentCollectionId,
			OrderByComparator<FragmentEntry> orderByComparator)
		throws NoSuchEntryException {

		return _collectionPersistenceFinderByFragmentCollectionId.findFirst(
			finderCache, new Object[] {fragmentCollectionId},
			orderByComparator);
	}

	/**
	 * Returns the first fragment entry in the ordered set where fragmentCollectionId = &#63;.
	 *
	 * @param fragmentCollectionId the fragment collection ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByFragmentCollectionId_First(
		long fragmentCollectionId,
		OrderByComparator<FragmentEntry> orderByComparator) {

		return _collectionPersistenceFinderByFragmentCollectionId.fetchFirst(
			finderCache, new Object[] {fragmentCollectionId},
			orderByComparator);
	}

	/**
	 * Removes all the fragment entries where fragmentCollectionId = &#63; from the database.
	 *
	 * @param fragmentCollectionId the fragment collection ID
	 */
	@Override
	public void removeByFragmentCollectionId(long fragmentCollectionId) {
		_collectionPersistenceFinderByFragmentCollectionId.remove(
			finderCache, new Object[] {fragmentCollectionId});
	}

	/**
	 * Returns the number of fragment entries where fragmentCollectionId = &#63;.
	 *
	 * @param fragmentCollectionId the fragment collection ID
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByFragmentCollectionId(long fragmentCollectionId) {
		return _collectionPersistenceFinderByFragmentCollectionId.count(
			finderCache, new Object[] {fragmentCollectionId});
	}

	private CollectionPersistenceFinder<FragmentEntry, NoSuchEntryException>
		_collectionPersistenceFinderByG_FCI;

	/**
	 * Returns an ordered range of all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByG_FCI(
		long groupId, long fragmentCollectionId, int start, int end,
		OrderByComparator<FragmentEntry> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByG_FCI.find(
			finderCache, new Object[] {groupId, fragmentCollectionId}, start,
			end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first fragment entry in the ordered set where groupId = &#63; and fragmentCollectionId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByG_FCI_First(
			long groupId, long fragmentCollectionId,
			OrderByComparator<FragmentEntry> orderByComparator)
		throws NoSuchEntryException {

		return _collectionPersistenceFinderByG_FCI.findFirst(
			finderCache, new Object[] {groupId, fragmentCollectionId},
			orderByComparator);
	}

	/**
	 * Returns the first fragment entry in the ordered set where groupId = &#63; and fragmentCollectionId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByG_FCI_First(
		long groupId, long fragmentCollectionId,
		OrderByComparator<FragmentEntry> orderByComparator) {

		return _collectionPersistenceFinderByG_FCI.fetchFirst(
			finderCache, new Object[] {groupId, fragmentCollectionId},
			orderByComparator);
	}

	/**
	 * Removes all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; from the database.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 */
	@Override
	public void removeByG_FCI(long groupId, long fragmentCollectionId) {
		_collectionPersistenceFinderByG_FCI.remove(
			finderCache, new Object[] {groupId, fragmentCollectionId});
	}

	/**
	 * Returns the number of fragment entries where groupId = &#63; and fragmentCollectionId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByG_FCI(long groupId, long fragmentCollectionId) {
		return _collectionPersistenceFinderByG_FCI.count(
			finderCache, new Object[] {groupId, fragmentCollectionId});
	}

	private UniquePersistenceFinder<FragmentEntry, NoSuchEntryException>
		_uniquePersistenceFinderByG_FEK_H;

	/**
	 * Returns the fragment entry where groupId = &#63; and fragmentEntryKey = &#63; and head = &#63; or throws a <code>NoSuchEntryException</code> if it could not be found.
	 *
	 * @param groupId the group ID
	 * @param fragmentEntryKey the fragment entry key
	 * @param head the head
	 * @return the matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByG_FEK_H(
			long groupId, String fragmentEntryKey, boolean head)
		throws NoSuchEntryException {

		return _uniquePersistenceFinderByG_FEK_H.find(
			finderCache, new Object[] {groupId, fragmentEntryKey, head});
	}

	/**
	 * Returns the fragment entry where groupId = &#63; and fragmentEntryKey = &#63; and head = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param groupId the group ID
	 * @param fragmentEntryKey the fragment entry key
	 * @param head the head
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByG_FEK_H(
		long groupId, String fragmentEntryKey, boolean head,
		boolean useFinderCache) {

		return _uniquePersistenceFinderByG_FEK_H.fetch(
			finderCache, new Object[] {groupId, fragmentEntryKey, head},
			useFinderCache);
	}

	/**
	 * Removes the fragment entry where groupId = &#63; and fragmentEntryKey = &#63; and head = &#63; from the database.
	 *
	 * @param groupId the group ID
	 * @param fragmentEntryKey the fragment entry key
	 * @param head the head
	 * @return the fragment entry that was removed
	 */
	@Override
	public FragmentEntry removeByG_FEK_H(
			long groupId, String fragmentEntryKey, boolean head)
		throws NoSuchEntryException {

		FragmentEntry fragmentEntry = findByG_FEK_H(
			groupId, fragmentEntryKey, head);

		return remove(fragmentEntry);
	}

	/**
	 * Returns the number of fragment entries where groupId = &#63; and fragmentEntryKey = &#63; and head = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentEntryKey the fragment entry key
	 * @param head the head
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByG_FEK_H(
		long groupId, String fragmentEntryKey, boolean head) {

		return _uniquePersistenceFinderByG_FEK_H.count(
			finderCache, new Object[] {groupId, fragmentEntryKey, head});
	}

	private CollectionPersistenceFinder<FragmentEntry, NoSuchEntryException>
		_collectionPersistenceFinderByG_FCI_LikeN;

	/**
	 * Returns all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @return the matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByG_FCI_LikeN(
		long groupId, long fragmentCollectionId, String name) {

		return findByG_FCI_LikeN(
			groupId, fragmentCollectionId, name, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @return the range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByG_FCI_LikeN(
		long groupId, long fragmentCollectionId, String name, int start,
		int end) {

		return findByG_FCI_LikeN(
			groupId, fragmentCollectionId, name, start, end, null);
	}

	/**
	 * Returns an ordered range of all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByG_FCI_LikeN(
		long groupId, long fragmentCollectionId, String name, int start,
		int end, OrderByComparator<FragmentEntry> orderByComparator) {

		return findByG_FCI_LikeN(
			groupId, fragmentCollectionId, name, start, end, orderByComparator,
			true);
	}

	/**
	 * Returns an ordered range of all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByG_FCI_LikeN(
		long groupId, long fragmentCollectionId, String name, int start,
		int end, OrderByComparator<FragmentEntry> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByG_FCI_LikeN.find(
			finderCache, new Object[] {groupId, fragmentCollectionId, name},
			start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first fragment entry in the ordered set where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByG_FCI_LikeN_First(
			long groupId, long fragmentCollectionId, String name,
			OrderByComparator<FragmentEntry> orderByComparator)
		throws NoSuchEntryException {

		return _collectionPersistenceFinderByG_FCI_LikeN.findFirst(
			finderCache, new Object[] {groupId, fragmentCollectionId, name},
			orderByComparator);
	}

	/**
	 * Returns the first fragment entry in the ordered set where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByG_FCI_LikeN_First(
		long groupId, long fragmentCollectionId, String name,
		OrderByComparator<FragmentEntry> orderByComparator) {

		return _collectionPersistenceFinderByG_FCI_LikeN.fetchFirst(
			finderCache, new Object[] {groupId, fragmentCollectionId, name},
			orderByComparator);
	}

	/**
	 * Removes all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63; from the database.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 */
	@Override
	public void removeByG_FCI_LikeN(
		long groupId, long fragmentCollectionId, String name) {

		_collectionPersistenceFinderByG_FCI_LikeN.remove(
			finderCache, new Object[] {groupId, fragmentCollectionId, name});
	}

	/**
	 * Returns the number of fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByG_FCI_LikeN(
		long groupId, long fragmentCollectionId, String name) {

		return _collectionPersistenceFinderByG_FCI_LikeN.count(
			finderCache, new Object[] {groupId, fragmentCollectionId, name});
	}

	private CollectionPersistenceFinder<FragmentEntry, NoSuchEntryException>
		_collectionPersistenceFinderByG_FCI_T;

	/**
	 * Returns an ordered range of all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and type = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param type the type
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByG_FCI_T(
		long groupId, long fragmentCollectionId, int type, int start, int end,
		OrderByComparator<FragmentEntry> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByG_FCI_T.find(
			finderCache, new Object[] {groupId, fragmentCollectionId, type},
			start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first fragment entry in the ordered set where groupId = &#63; and fragmentCollectionId = &#63; and type = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param type the type
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByG_FCI_T_First(
			long groupId, long fragmentCollectionId, int type,
			OrderByComparator<FragmentEntry> orderByComparator)
		throws NoSuchEntryException {

		return _collectionPersistenceFinderByG_FCI_T.findFirst(
			finderCache, new Object[] {groupId, fragmentCollectionId, type},
			orderByComparator);
	}

	/**
	 * Returns the first fragment entry in the ordered set where groupId = &#63; and fragmentCollectionId = &#63; and type = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param type the type
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByG_FCI_T_First(
		long groupId, long fragmentCollectionId, int type,
		OrderByComparator<FragmentEntry> orderByComparator) {

		return _collectionPersistenceFinderByG_FCI_T.fetchFirst(
			finderCache, new Object[] {groupId, fragmentCollectionId, type},
			orderByComparator);
	}

	/**
	 * Removes all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and type = &#63; from the database.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param type the type
	 */
	@Override
	public void removeByG_FCI_T(
		long groupId, long fragmentCollectionId, int type) {

		_collectionPersistenceFinderByG_FCI_T.remove(
			finderCache, new Object[] {groupId, fragmentCollectionId, type});
	}

	/**
	 * Returns the number of fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and type = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param type the type
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByG_FCI_T(
		long groupId, long fragmentCollectionId, int type) {

		return _collectionPersistenceFinderByG_FCI_T.count(
			finderCache, new Object[] {groupId, fragmentCollectionId, type});
	}

	private CollectionPersistenceFinder<FragmentEntry, NoSuchEntryException>
		_collectionPersistenceFinderByG_FCI_S;

	/**
	 * Returns an ordered range of all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and status = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param status the status
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByG_FCI_S(
		long groupId, long fragmentCollectionId, int status, int start, int end,
		OrderByComparator<FragmentEntry> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByG_FCI_S.find(
			finderCache, new Object[] {groupId, fragmentCollectionId, status},
			start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first fragment entry in the ordered set where groupId = &#63; and fragmentCollectionId = &#63; and status = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param status the status
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByG_FCI_S_First(
			long groupId, long fragmentCollectionId, int status,
			OrderByComparator<FragmentEntry> orderByComparator)
		throws NoSuchEntryException {

		return _collectionPersistenceFinderByG_FCI_S.findFirst(
			finderCache, new Object[] {groupId, fragmentCollectionId, status},
			orderByComparator);
	}

	/**
	 * Returns the first fragment entry in the ordered set where groupId = &#63; and fragmentCollectionId = &#63; and status = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param status the status
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByG_FCI_S_First(
		long groupId, long fragmentCollectionId, int status,
		OrderByComparator<FragmentEntry> orderByComparator) {

		return _collectionPersistenceFinderByG_FCI_S.fetchFirst(
			finderCache, new Object[] {groupId, fragmentCollectionId, status},
			orderByComparator);
	}

	/**
	 * Removes all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and status = &#63; from the database.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param status the status
	 */
	@Override
	public void removeByG_FCI_S(
		long groupId, long fragmentCollectionId, int status) {

		_collectionPersistenceFinderByG_FCI_S.remove(
			finderCache, new Object[] {groupId, fragmentCollectionId, status});
	}

	/**
	 * Returns the number of fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and status = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param status the status
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByG_FCI_S(
		long groupId, long fragmentCollectionId, int status) {

		return _collectionPersistenceFinderByG_FCI_S.count(
			finderCache, new Object[] {groupId, fragmentCollectionId, status});
	}

	private CollectionPersistenceFinder<FragmentEntry, NoSuchEntryException>
		_collectionPersistenceFinderByG_FCI_LikeN_S;

	/**
	 * Returns all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63; and status = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param status the status
	 * @return the matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByG_FCI_LikeN_S(
		long groupId, long fragmentCollectionId, String name, int status) {

		return findByG_FCI_LikeN_S(
			groupId, fragmentCollectionId, name, status, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63; and status = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param status the status
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @return the range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByG_FCI_LikeN_S(
		long groupId, long fragmentCollectionId, String name, int status,
		int start, int end) {

		return findByG_FCI_LikeN_S(
			groupId, fragmentCollectionId, name, status, start, end, null);
	}

	/**
	 * Returns an ordered range of all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63; and status = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param status the status
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByG_FCI_LikeN_S(
		long groupId, long fragmentCollectionId, String name, int status,
		int start, int end,
		OrderByComparator<FragmentEntry> orderByComparator) {

		return findByG_FCI_LikeN_S(
			groupId, fragmentCollectionId, name, status, start, end,
			orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63; and status = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param status the status
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByG_FCI_LikeN_S(
		long groupId, long fragmentCollectionId, String name, int status,
		int start, int end, OrderByComparator<FragmentEntry> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByG_FCI_LikeN_S.find(
			finderCache,
			new Object[] {groupId, fragmentCollectionId, name, status}, start,
			end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first fragment entry in the ordered set where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63; and status = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param status the status
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByG_FCI_LikeN_S_First(
			long groupId, long fragmentCollectionId, String name, int status,
			OrderByComparator<FragmentEntry> orderByComparator)
		throws NoSuchEntryException {

		return _collectionPersistenceFinderByG_FCI_LikeN_S.findFirst(
			finderCache,
			new Object[] {groupId, fragmentCollectionId, name, status},
			orderByComparator);
	}

	/**
	 * Returns the first fragment entry in the ordered set where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63; and status = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param status the status
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByG_FCI_LikeN_S_First(
		long groupId, long fragmentCollectionId, String name, int status,
		OrderByComparator<FragmentEntry> orderByComparator) {

		return _collectionPersistenceFinderByG_FCI_LikeN_S.fetchFirst(
			finderCache,
			new Object[] {groupId, fragmentCollectionId, name, status},
			orderByComparator);
	}

	/**
	 * Removes all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63; and status = &#63; from the database.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param status the status
	 */
	@Override
	public void removeByG_FCI_LikeN_S(
		long groupId, long fragmentCollectionId, String name, int status) {

		_collectionPersistenceFinderByG_FCI_LikeN_S.remove(
			finderCache,
			new Object[] {groupId, fragmentCollectionId, name, status});
	}

	/**
	 * Returns the number of fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and name LIKE &#63; and status = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param name the name
	 * @param status the status
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByG_FCI_LikeN_S(
		long groupId, long fragmentCollectionId, String name, int status) {

		return _collectionPersistenceFinderByG_FCI_LikeN_S.count(
			finderCache,
			new Object[] {groupId, fragmentCollectionId, name, status});
	}

	private CollectionPersistenceFinder<FragmentEntry, NoSuchEntryException>
		_collectionPersistenceFinderByG_FCI_T_S;

	/**
	 * Returns an ordered range of all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and type = &#63; and status = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>FragmentEntryModelImpl</code>.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param type the type
	 * @param status the status
	 * @param start the lower bound of the range of fragment entries
	 * @param end the upper bound of the range of fragment entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching fragment entries
	 */
	@Override
	public List<FragmentEntry> findByG_FCI_T_S(
		long groupId, long fragmentCollectionId, int type, int status,
		int start, int end, OrderByComparator<FragmentEntry> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByG_FCI_T_S.find(
			finderCache,
			new Object[] {groupId, fragmentCollectionId, type, status}, start,
			end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first fragment entry in the ordered set where groupId = &#63; and fragmentCollectionId = &#63; and type = &#63; and status = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param type the type
	 * @param status the status
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByG_FCI_T_S_First(
			long groupId, long fragmentCollectionId, int type, int status,
			OrderByComparator<FragmentEntry> orderByComparator)
		throws NoSuchEntryException {

		return _collectionPersistenceFinderByG_FCI_T_S.findFirst(
			finderCache,
			new Object[] {groupId, fragmentCollectionId, type, status},
			orderByComparator);
	}

	/**
	 * Returns the first fragment entry in the ordered set where groupId = &#63; and fragmentCollectionId = &#63; and type = &#63; and status = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param type the type
	 * @param status the status
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByG_FCI_T_S_First(
		long groupId, long fragmentCollectionId, int type, int status,
		OrderByComparator<FragmentEntry> orderByComparator) {

		return _collectionPersistenceFinderByG_FCI_T_S.fetchFirst(
			finderCache,
			new Object[] {groupId, fragmentCollectionId, type, status},
			orderByComparator);
	}

	/**
	 * Removes all the fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and type = &#63; and status = &#63; from the database.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param type the type
	 * @param status the status
	 */
	@Override
	public void removeByG_FCI_T_S(
		long groupId, long fragmentCollectionId, int type, int status) {

		_collectionPersistenceFinderByG_FCI_T_S.remove(
			finderCache,
			new Object[] {groupId, fragmentCollectionId, type, status});
	}

	/**
	 * Returns the number of fragment entries where groupId = &#63; and fragmentCollectionId = &#63; and type = &#63; and status = &#63;.
	 *
	 * @param groupId the group ID
	 * @param fragmentCollectionId the fragment collection ID
	 * @param type the type
	 * @param status the status
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByG_FCI_T_S(
		long groupId, long fragmentCollectionId, int type, int status) {

		return _collectionPersistenceFinderByG_FCI_T_S.count(
			finderCache,
			new Object[] {groupId, fragmentCollectionId, type, status});
	}

	private UniquePersistenceFinder<FragmentEntry, NoSuchEntryException>
		_uniquePersistenceFinderByERC_G;

	/**
	 * Returns the fragment entry where externalReferenceCode = &#63; and groupId = &#63; or throws a <code>NoSuchEntryException</code> if it could not be found.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param groupId the group ID
	 * @return the matching fragment entry
	 * @throws NoSuchEntryException if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry findByERC_G(String externalReferenceCode, long groupId)
		throws NoSuchEntryException {

		return _uniquePersistenceFinderByERC_G.find(
			finderCache, new Object[] {externalReferenceCode, groupId});
	}

	/**
	 * Returns the fragment entry where externalReferenceCode = &#63; and groupId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param groupId the group ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching fragment entry, or <code>null</code> if a matching fragment entry could not be found
	 */
	@Override
	public FragmentEntry fetchByERC_G(
		String externalReferenceCode, long groupId, boolean useFinderCache) {

		return _uniquePersistenceFinderByERC_G.fetch(
			finderCache, new Object[] {externalReferenceCode, groupId},
			useFinderCache);
	}

	/**
	 * Removes the fragment entry where externalReferenceCode = &#63; and groupId = &#63; from the database.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param groupId the group ID
	 * @return the fragment entry that was removed
	 */
	@Override
	public FragmentEntry removeByERC_G(
			String externalReferenceCode, long groupId)
		throws NoSuchEntryException {

		FragmentEntry fragmentEntry = findByERC_G(
			externalReferenceCode, groupId);

		return remove(fragmentEntry);
	}

	/**
	 * Returns the number of fragment entries where externalReferenceCode = &#63; and groupId = &#63;.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param groupId the group ID
	 * @return the number of matching fragment entries
	 */
	@Override
	public int countByERC_G(String externalReferenceCode, long groupId) {
		return _uniquePersistenceFinderByERC_G.count(
			finderCache, new Object[] {externalReferenceCode, groupId});
	}

	public FragmentEntryPersistenceImpl() {
		Map<String, String> dbColumnNames = new HashMap<String, String>();

		dbColumnNames.put("uuid", "uuid_");
		dbColumnNames.put("type", "type_");

		setDBColumnNames(dbColumnNames);

		setModelClass(FragmentEntry.class);

		setModelImplClass(FragmentEntryImpl.class);
		setModelPKClass(long.class);

		setTable(FragmentEntryTable.INSTANCE);
	}

	/**
	 * Creates a new fragment entry with the primary key. Does not add the fragment entry to the database.
	 *
	 * @param fragmentEntryId the primary key for the new fragment entry
	 * @return the new fragment entry
	 */
	@Override
	public FragmentEntry create(long fragmentEntryId) {
		FragmentEntry fragmentEntry = new FragmentEntryImpl();

		fragmentEntry.setNew(true);
		fragmentEntry.setPrimaryKey(fragmentEntryId);

		String uuid = PortalUUIDUtil.generate();

		fragmentEntry.setUuid(uuid);

		fragmentEntry.setCompanyId(CompanyThreadLocal.getCompanyId());

		return fragmentEntry;
	}

	/**
	 * Removes the fragment entry with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param fragmentEntryId the primary key of the fragment entry
	 * @return the fragment entry that was removed
	 * @throws NoSuchEntryException if a fragment entry with the primary key could not be found
	 */
	@Override
	public FragmentEntry remove(long fragmentEntryId)
		throws NoSuchEntryException {

		return remove((Serializable)fragmentEntryId);
	}

	@Override
	protected FragmentEntry removeImpl(FragmentEntry fragmentEntry) {
		Session session = null;

		try {
			session = openSession();

			if (!session.contains(fragmentEntry)) {
				fragmentEntry = (FragmentEntry)session.get(
					FragmentEntryImpl.class, fragmentEntry.getPrimaryKeyObj());
			}

			if ((fragmentEntry != null) &&
				ctPersistenceHelper.isRemove(fragmentEntry)) {

				session.delete(fragmentEntry);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		if (fragmentEntry != null) {
			clearCache(fragmentEntry);
		}

		return fragmentEntry;
	}

	@Override
	public FragmentEntry updateImpl(FragmentEntry fragmentEntry) {
		boolean isNew = fragmentEntry.isNew();

		if (!(fragmentEntry instanceof FragmentEntryModelImpl)) {
			InvocationHandler invocationHandler = null;

			if (ProxyUtil.isProxyClass(fragmentEntry.getClass())) {
				invocationHandler = ProxyUtil.getInvocationHandler(
					fragmentEntry);

				throw new IllegalArgumentException(
					"Implement ModelWrapper in fragmentEntry proxy " +
						invocationHandler.getClass());
			}

			throw new IllegalArgumentException(
				"Implement ModelWrapper in custom FragmentEntry implementation " +
					fragmentEntry.getClass());
		}

		FragmentEntryModelImpl fragmentEntryModelImpl =
			(FragmentEntryModelImpl)fragmentEntry;

		if (Validator.isNull(fragmentEntry.getUuid())) {
			String uuid = PortalUUIDUtil.generate();

			fragmentEntry.setUuid(uuid);
		}

		if (Validator.isNull(fragmentEntry.getExternalReferenceCode())) {
			fragmentEntry.setExternalReferenceCode(fragmentEntry.getUuid());
		}
		else {
			if (!Objects.equals(
					fragmentEntry.getUuid(),
					fragmentEntry.getExternalReferenceCode()) &&
				!Objects.equals(
					fragmentEntryModelImpl.getColumnOriginalValue(
						"externalReferenceCode"),
					fragmentEntry.getExternalReferenceCode())) {

				long userId = GetterUtil.getLong(
					PrincipalThreadLocal.getName());

				if (userId > 0) {
					long companyId = fragmentEntry.getCompanyId();

					long groupId = fragmentEntry.getGroupId();

					long classPK = 0;

					if (!isNew) {
						classPK = fragmentEntry.getPrimaryKey();
					}

					try {
						fragmentEntry.setExternalReferenceCode(
							SanitizerUtil.sanitize(
								companyId, groupId, userId,
								FragmentEntry.class.getName(), classPK,
								ContentTypes.TEXT_HTML, Sanitizer.MODE_ALL,
								fragmentEntry.getExternalReferenceCode(),
								null));
					}
					catch (SanitizerException sanitizerException) {
						throw new SystemException(sanitizerException);
					}
				}
			}

			FragmentEntry ercFragmentEntry = fetchByERC_G(
				fragmentEntry.getExternalReferenceCode(),
				fragmentEntry.getGroupId());

			if (isNew) {
				if (ercFragmentEntry != null) {
					throw new DuplicateFragmentEntryExternalReferenceCodeException(
						"Duplicate fragment entry with external reference code " +
							fragmentEntry.getExternalReferenceCode() +
								" and group " + fragmentEntry.getGroupId());
				}
			}
			else {
				if ((ercFragmentEntry != null) &&
					(fragmentEntry.getFragmentEntryId() !=
						ercFragmentEntry.getFragmentEntryId())) {

					throw new DuplicateFragmentEntryExternalReferenceCodeException(
						"Duplicate fragment entry with external reference code " +
							fragmentEntry.getExternalReferenceCode() +
								" and group " + fragmentEntry.getGroupId());
				}
			}
		}

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		Date date = new Date();

		if (isNew && (fragmentEntry.getCreateDate() == null)) {
			if (serviceContext == null) {
				fragmentEntry.setCreateDate(date);
			}
			else {
				fragmentEntry.setCreateDate(serviceContext.getCreateDate(date));
			}
		}

		if (!fragmentEntryModelImpl.hasSetModifiedDate()) {
			if (serviceContext == null) {
				fragmentEntry.setModifiedDate(date);
			}
			else {
				fragmentEntry.setModifiedDate(
					serviceContext.getModifiedDate(date));
			}
		}

		Session session = null;

		try {
			session = openSession();

			if (ctPersistenceHelper.isInsert(fragmentEntry)) {
				if (!isNew) {
					session.evict(
						FragmentEntryImpl.class,
						fragmentEntry.getPrimaryKeyObj());
				}

				session.save(fragmentEntry);
			}
			else {
				fragmentEntry = (FragmentEntry)session.merge(fragmentEntry);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		cacheUniqueFindersResult(fragmentEntry, false);

		if (isNew) {
			fragmentEntry.setNew(false);
		}

		fragmentEntry.resetOriginalValues();

		return fragmentEntry;
	}

	/**
	 * Returns the fragment entry with the primary key or throws a <code>NoSuchEntryException</code> if it could not be found.
	 *
	 * @param fragmentEntryId the primary key of the fragment entry
	 * @return the fragment entry
	 * @throws NoSuchEntryException if a fragment entry with the primary key could not be found
	 */
	@Override
	public FragmentEntry findByPrimaryKey(long fragmentEntryId)
		throws NoSuchEntryException {

		return findByPrimaryKey((Serializable)fragmentEntryId);
	}

	@Override
	protected CTPersistenceHelper getCTPersistenceHelper() {
		return ctPersistenceHelper;
	}

	/**
	 * Returns the fragment entry with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param fragmentEntryId the primary key of the fragment entry
	 * @return the fragment entry, or <code>null</code> if a fragment entry with the primary key could not be found
	 */
	@Override
	public FragmentEntry fetchByPrimaryKey(long fragmentEntryId) {
		return fetchByPrimaryKey((Serializable)fragmentEntryId);
	}

	@Override
	public Set<String> getBadColumnNames() {
		return _badColumnNames;
	}

	@Override
	protected EntityCache getEntityCache() {
		return entityCache;
	}

	@Override
	protected String getPKDBName() {
		return "fragmentEntryId";
	}

	@Override
	protected String getSelectSQL() {
		return _SQL_SELECT_FRAGMENTENTRY;
	}

	@Override
	public Set<String> getCTColumnNames(
		CTColumnResolutionType ctColumnResolutionType) {

		return _ctColumnNamesMap.getOrDefault(
			ctColumnResolutionType, Collections.emptySet());
	}

	@Override
	public List<String> getMappingTableNames() {
		return _mappingTableNames;
	}

	@Override
	public Map<String, Integer> getTableColumnsMap() {
		return FragmentEntryModelImpl.TABLE_COLUMNS_MAP;
	}

	@Override
	public String getTableName() {
		return "FragmentEntry";
	}

	@Override
	public List<String[]> getUniqueIndexColumnNames() {
		return _uniqueIndexColumnNames;
	}

	private static final Map<CTColumnResolutionType, Set<String>>
		_ctColumnNamesMap = new EnumMap<CTColumnResolutionType, Set<String>>(
			CTColumnResolutionType.class);
	private static final List<String> _mappingTableNames =
		new ArrayList<String>();
	private static final List<String[]> _uniqueIndexColumnNames =
		new ArrayList<String[]>();

	static {
		Set<String> ctControlColumnNames = new HashSet<String>();
		Set<String> ctIgnoreColumnNames = new HashSet<String>();
		Set<String> ctMergeColumnNames = new HashSet<String>();
		Set<String> ctStrictColumnNames = new HashSet<String>();

		ctControlColumnNames.add("mvccVersion");
		ctControlColumnNames.add("ctCollectionId");
		ctStrictColumnNames.add("uuid_");
		ctStrictColumnNames.add("externalReferenceCode");
		ctMergeColumnNames.add("headId");
		ctMergeColumnNames.add("head");
		ctStrictColumnNames.add("groupId");
		ctStrictColumnNames.add("companyId");
		ctStrictColumnNames.add("userId");
		ctStrictColumnNames.add("userName");
		ctStrictColumnNames.add("createDate");
		ctIgnoreColumnNames.add("modifiedDate");
		ctMergeColumnNames.add("fragmentCollectionId");
		ctMergeColumnNames.add("fragmentEntryKey");
		ctMergeColumnNames.add("name");
		ctMergeColumnNames.add("css");
		ctMergeColumnNames.add("html");
		ctMergeColumnNames.add("js");
		ctMergeColumnNames.add("cacheable");
		ctMergeColumnNames.add("configuration");
		ctMergeColumnNames.add("icon");
		ctMergeColumnNames.add("previewFileEntryId");
		ctMergeColumnNames.add("marketplace");
		ctMergeColumnNames.add("readOnly");
		ctMergeColumnNames.add("type_");
		ctMergeColumnNames.add("typeOptions");
		ctMergeColumnNames.add("lastPublishDate");
		ctMergeColumnNames.add("status");
		ctMergeColumnNames.add("statusByUserId");
		ctMergeColumnNames.add("statusByUserName");
		ctMergeColumnNames.add("statusDate");

		_ctColumnNamesMap.put(
			CTColumnResolutionType.CONTROL, ctControlColumnNames);
		_ctColumnNamesMap.put(
			CTColumnResolutionType.IGNORE, ctIgnoreColumnNames);
		_ctColumnNamesMap.put(CTColumnResolutionType.MERGE, ctMergeColumnNames);
		_ctColumnNamesMap.put(
			CTColumnResolutionType.PK,
			Collections.singleton("fragmentEntryId"));
		_ctColumnNamesMap.put(
			CTColumnResolutionType.STRICT, ctStrictColumnNames);

		_uniqueIndexColumnNames.add(new String[] {"uuid_", "groupId"});

		_uniqueIndexColumnNames.add(new String[] {"headId"});

		_uniqueIndexColumnNames.add(
			new String[] {"groupId", "fragmentEntryKey", "head"});

		_uniqueIndexColumnNames.add(
			new String[] {"externalReferenceCode", "groupId"});
	}

	/**
	 * Initializes the fragment entry persistence.
	 */
	@Activate
	public void activate() {
		_collectionPersistenceFinderByUuid = new CollectionPersistenceFinder<>(
			this,
			new FinderPath(
				FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByUuid",
				new String[] {
					String.class.getName(), Integer.class.getName(),
					Integer.class.getName(), OrderByComparator.class.getName()
				},
				new String[] {"uuid_"}, true),
			new FinderPath(
				FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByUuid",
				new String[] {String.class.getName()}, new String[] {"uuid_"},
				0, 1, true, null),
			new FinderPath(
				FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByUuid",
				new String[] {String.class.getName()}, new String[] {"uuid_"},
				0, 1, false, null),
			_SQL_SELECT_FRAGMENTENTRY_WHERE, _SQL_COUNT_FRAGMENTENTRY_WHERE,
			FragmentEntryModelImpl.ORDER_BY_JPQL, _ENTITY_ALIAS_PREFIX, "", "",
			null,
			new FinderColumn<>(
				"fragmentEntry.", "uuid", "uuid_", FinderColumn.Type.STRING,
				"=", true, true, FragmentEntry::getUuid));

		_uniquePersistenceFinderByUUID_G = new UniquePersistenceFinder<>(
			this,
			createUniqueFinderPath(
				FINDER_CLASS_NAME_ENTITY, "fetchByUUID_G",
				new String[] {String.class.getName(), Long.class.getName()},
				new String[] {"uuid_", "groupId"}, 0, 1, false,
				convertNullFunction(FragmentEntry::getUuid),
				FragmentEntry::getGroupId),
			_SQL_SELECT_FRAGMENTENTRY_WHERE, "",
			new FinderColumn<>(
				"fragmentEntry.", "uuid", "uuid_", FinderColumn.Type.STRING,
				"=", true, true, FragmentEntry::getUuid),
			new FinderColumn<>(
				"fragmentEntry.", "groupId", FinderColumn.Type.LONG, "=", true,
				true, FragmentEntry::getGroupId));

		_collectionPersistenceFinderByUuid_C =
			new CollectionPersistenceFinder<>(
				this,
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByUuid_C",
					new String[] {
						String.class.getName(), Long.class.getName(),
						Integer.class.getName(), Integer.class.getName(),
						OrderByComparator.class.getName()
					},
					new String[] {"uuid_", "companyId"}, true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByUuid_C",
					new String[] {String.class.getName(), Long.class.getName()},
					new String[] {"uuid_", "companyId"}, 0, 1, true, null),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByUuid_C",
					new String[] {String.class.getName(), Long.class.getName()},
					new String[] {"uuid_", "companyId"}, 0, 1, false, null),
				_SQL_SELECT_FRAGMENTENTRY_WHERE, _SQL_COUNT_FRAGMENTENTRY_WHERE,
				FragmentEntryModelImpl.ORDER_BY_JPQL, _ENTITY_ALIAS_PREFIX, "",
				"", null,
				new FinderColumn<>(
					"fragmentEntry.", "uuid", "uuid_", FinderColumn.Type.STRING,
					"=", true, true, FragmentEntry::getUuid),
				new FinderColumn<>(
					"fragmentEntry.", "companyId", FinderColumn.Type.LONG, "=",
					true, true, FragmentEntry::getCompanyId));

		_uniquePersistenceFinderByHeadId = new UniquePersistenceFinder<>(
			this,
			createUniqueFinderPath(
				FINDER_CLASS_NAME_ENTITY, "fetchByHeadId",
				new String[] {Long.class.getName()}, new String[] {"headId"}, 0,
				0, false, FragmentEntry::getHeadId),
			_SQL_SELECT_FRAGMENTENTRY_WHERE, "",
			new FinderColumn<>(
				"fragmentEntry.", "headId", FinderColumn.Type.LONG, "=", true,
				true, FragmentEntry::getHeadId));

		_collectionPersistenceFinderByFragmentCollectionId =
			new CollectionPersistenceFinder<>(
				this,
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
					"findByFragmentCollectionId",
					new String[] {
						Long.class.getName(), Integer.class.getName(),
						Integer.class.getName(),
						OrderByComparator.class.getName()
					},
					new String[] {"fragmentCollectionId"}, true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
					"findByFragmentCollectionId",
					new String[] {Long.class.getName()},
					new String[] {"fragmentCollectionId"}, true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
					"countByFragmentCollectionId",
					new String[] {Long.class.getName()},
					new String[] {"fragmentCollectionId"}, false),
				_SQL_SELECT_FRAGMENTENTRY_WHERE, _SQL_COUNT_FRAGMENTENTRY_WHERE,
				FragmentEntryModelImpl.ORDER_BY_JPQL, _ENTITY_ALIAS_PREFIX, "",
				"", null,
				new FinderColumn<>(
					"fragmentEntry.", "fragmentCollectionId",
					FinderColumn.Type.LONG, "=", true, true,
					FragmentEntry::getFragmentCollectionId));

		_collectionPersistenceFinderByG_FCI = new CollectionPersistenceFinder<>(
			this,
			new FinderPath(
				FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByG_FCI",
				new String[] {
					Long.class.getName(), Long.class.getName(),
					Integer.class.getName(), Integer.class.getName(),
					OrderByComparator.class.getName()
				},
				new String[] {"groupId", "fragmentCollectionId"}, true),
			new FinderPath(
				FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByG_FCI",
				new String[] {Long.class.getName(), Long.class.getName()},
				new String[] {"groupId", "fragmentCollectionId"}, true),
			new FinderPath(
				FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByG_FCI",
				new String[] {Long.class.getName(), Long.class.getName()},
				new String[] {"groupId", "fragmentCollectionId"}, false),
			_SQL_SELECT_FRAGMENTENTRY_WHERE, _SQL_COUNT_FRAGMENTENTRY_WHERE,
			FragmentEntryModelImpl.ORDER_BY_JPQL, _ENTITY_ALIAS_PREFIX, "", "",
			null,
			new FinderColumn<>(
				"fragmentEntry.", "groupId", FinderColumn.Type.LONG, "=", true,
				true, FragmentEntry::getGroupId),
			new FinderColumn<>(
				"fragmentEntry.", "fragmentCollectionId",
				FinderColumn.Type.LONG, "=", true, true,
				FragmentEntry::getFragmentCollectionId));

		_uniquePersistenceFinderByG_FEK_H = new UniquePersistenceFinder<>(
			this,
			createUniqueFinderPath(
				FINDER_CLASS_NAME_ENTITY, "fetchByG_FEK_H",
				new String[] {
					Long.class.getName(), String.class.getName(),
					Boolean.class.getName()
				},
				new String[] {"groupId", "fragmentEntryKey", "head"}, 0, 2,
				false, FragmentEntry::getGroupId,
				convertNullFunction(FragmentEntry::getFragmentEntryKey),
				FragmentEntry::isHead),
			_SQL_SELECT_FRAGMENTENTRY_WHERE, "",
			new FinderColumn<>(
				"fragmentEntry.", "groupId", FinderColumn.Type.LONG, "=", true,
				true, FragmentEntry::getGroupId),
			new FinderColumn<>(
				"fragmentEntry.", "fragmentEntryKey", FinderColumn.Type.STRING,
				"=", true, true, FragmentEntry::getFragmentEntryKey),
			new FinderColumn<>(
				"fragmentEntry.", "head", FinderColumn.Type.BOOLEAN, "=", true,
				true, FragmentEntry::isHead));

		_collectionPersistenceFinderByG_FCI_LikeN =
			new CollectionPersistenceFinder<>(
				this,
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByG_FCI_LikeN",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						String.class.getName(), Integer.class.getName(),
						Integer.class.getName(),
						OrderByComparator.class.getName()
					},
					new String[] {"groupId", "fragmentCollectionId", "name"},
					true),
				null,
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
					"countByG_FCI_LikeN",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						String.class.getName()
					},
					new String[] {"groupId", "fragmentCollectionId", "name"},
					false),
				_SQL_SELECT_FRAGMENTENTRY_WHERE, _SQL_COUNT_FRAGMENTENTRY_WHERE,
				FragmentEntryModelImpl.ORDER_BY_JPQL, _ENTITY_ALIAS_PREFIX, "",
				"", null,
				new FinderColumn<>(
					"fragmentEntry.", "groupId", FinderColumn.Type.LONG, "=",
					true, true, FragmentEntry::getGroupId),
				new FinderColumn<>(
					"fragmentEntry.", "fragmentCollectionId",
					FinderColumn.Type.LONG, "=", true, true,
					FragmentEntry::getFragmentCollectionId),
				new FinderColumn<>(
					"fragmentEntry.", "name", FinderColumn.Type.STRING, "LIKE",
					true, true, FragmentEntry::getName));

		_collectionPersistenceFinderByG_FCI_T =
			new CollectionPersistenceFinder<>(
				this,
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByG_FCI_T",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						Integer.class.getName(), Integer.class.getName(),
						Integer.class.getName(),
						OrderByComparator.class.getName()
					},
					new String[] {"groupId", "fragmentCollectionId", "type_"},
					true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByG_FCI_T",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						Integer.class.getName()
					},
					new String[] {"groupId", "fragmentCollectionId", "type_"},
					true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByG_FCI_T",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						Integer.class.getName()
					},
					new String[] {"groupId", "fragmentCollectionId", "type_"},
					false),
				_SQL_SELECT_FRAGMENTENTRY_WHERE, _SQL_COUNT_FRAGMENTENTRY_WHERE,
				FragmentEntryModelImpl.ORDER_BY_JPQL, _ENTITY_ALIAS_PREFIX, "",
				"", null,
				new FinderColumn<>(
					"fragmentEntry.", "groupId", FinderColumn.Type.LONG, "=",
					true, true, FragmentEntry::getGroupId),
				new FinderColumn<>(
					"fragmentEntry.", "fragmentCollectionId",
					FinderColumn.Type.LONG, "=", true, true,
					FragmentEntry::getFragmentCollectionId),
				new FinderColumn<>(
					"fragmentEntry.", "type", "type_",
					FinderColumn.Type.INTEGER, "=", true, true,
					FragmentEntry::getType));

		_collectionPersistenceFinderByG_FCI_S =
			new CollectionPersistenceFinder<>(
				this,
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByG_FCI_S",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						Integer.class.getName(), Integer.class.getName(),
						Integer.class.getName(),
						OrderByComparator.class.getName()
					},
					new String[] {"groupId", "fragmentCollectionId", "status"},
					true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByG_FCI_S",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						Integer.class.getName()
					},
					new String[] {"groupId", "fragmentCollectionId", "status"},
					true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByG_FCI_S",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						Integer.class.getName()
					},
					new String[] {"groupId", "fragmentCollectionId", "status"},
					false),
				_SQL_SELECT_FRAGMENTENTRY_WHERE, _SQL_COUNT_FRAGMENTENTRY_WHERE,
				FragmentEntryModelImpl.ORDER_BY_JPQL, _ENTITY_ALIAS_PREFIX, "",
				"", null,
				new FinderColumn<>(
					"fragmentEntry.", "groupId", FinderColumn.Type.LONG, "=",
					true, true, FragmentEntry::getGroupId),
				new FinderColumn<>(
					"fragmentEntry.", "fragmentCollectionId",
					FinderColumn.Type.LONG, "=", true, true,
					FragmentEntry::getFragmentCollectionId),
				new FinderColumn<>(
					"fragmentEntry.", "status", FinderColumn.Type.INTEGER, "=",
					true, true, FragmentEntry::getStatus));

		_collectionPersistenceFinderByG_FCI_LikeN_S =
			new CollectionPersistenceFinder<>(
				this,
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
					"findByG_FCI_LikeN_S",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						String.class.getName(), Integer.class.getName(),
						Integer.class.getName(), Integer.class.getName(),
						OrderByComparator.class.getName()
					},
					new String[] {
						"groupId", "fragmentCollectionId", "name", "status"
					},
					true),
				null,
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
					"countByG_FCI_LikeN_S",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						String.class.getName(), Integer.class.getName()
					},
					new String[] {
						"groupId", "fragmentCollectionId", "name", "status"
					},
					false),
				_SQL_SELECT_FRAGMENTENTRY_WHERE, _SQL_COUNT_FRAGMENTENTRY_WHERE,
				FragmentEntryModelImpl.ORDER_BY_JPQL, _ENTITY_ALIAS_PREFIX, "",
				"", null,
				new FinderColumn<>(
					"fragmentEntry.", "groupId", FinderColumn.Type.LONG, "=",
					true, true, FragmentEntry::getGroupId),
				new FinderColumn<>(
					"fragmentEntry.", "fragmentCollectionId",
					FinderColumn.Type.LONG, "=", true, true,
					FragmentEntry::getFragmentCollectionId),
				new FinderColumn<>(
					"fragmentEntry.", "name", FinderColumn.Type.STRING, "LIKE",
					true, true, FragmentEntry::getName),
				new FinderColumn<>(
					"fragmentEntry.", "status", FinderColumn.Type.INTEGER, "=",
					true, true, FragmentEntry::getStatus));

		_collectionPersistenceFinderByG_FCI_T_S =
			new CollectionPersistenceFinder<>(
				this,
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByG_FCI_T_S",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						Integer.class.getName(), Integer.class.getName(),
						Integer.class.getName(), Integer.class.getName(),
						OrderByComparator.class.getName()
					},
					new String[] {
						"groupId", "fragmentCollectionId", "type_", "status"
					},
					true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
					"findByG_FCI_T_S",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						Integer.class.getName(), Integer.class.getName()
					},
					new String[] {
						"groupId", "fragmentCollectionId", "type_", "status"
					},
					true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
					"countByG_FCI_T_S",
					new String[] {
						Long.class.getName(), Long.class.getName(),
						Integer.class.getName(), Integer.class.getName()
					},
					new String[] {
						"groupId", "fragmentCollectionId", "type_", "status"
					},
					false),
				_SQL_SELECT_FRAGMENTENTRY_WHERE, _SQL_COUNT_FRAGMENTENTRY_WHERE,
				FragmentEntryModelImpl.ORDER_BY_JPQL, _ENTITY_ALIAS_PREFIX, "",
				"", null,
				new FinderColumn<>(
					"fragmentEntry.", "groupId", FinderColumn.Type.LONG, "=",
					true, true, FragmentEntry::getGroupId),
				new FinderColumn<>(
					"fragmentEntry.", "fragmentCollectionId",
					FinderColumn.Type.LONG, "=", true, true,
					FragmentEntry::getFragmentCollectionId),
				new FinderColumn<>(
					"fragmentEntry.", "type", "type_",
					FinderColumn.Type.INTEGER, "=", true, true,
					FragmentEntry::getType),
				new FinderColumn<>(
					"fragmentEntry.", "status", FinderColumn.Type.INTEGER, "=",
					true, true, FragmentEntry::getStatus));

		_uniquePersistenceFinderByERC_G = new UniquePersistenceFinder<>(
			this,
			createUniqueFinderPath(
				FINDER_CLASS_NAME_ENTITY, "fetchByERC_G",
				new String[] {String.class.getName(), Long.class.getName()},
				new String[] {"externalReferenceCode", "groupId"}, 0, 1, false,
				convertNullFunction(FragmentEntry::getExternalReferenceCode),
				FragmentEntry::getGroupId),
			_SQL_SELECT_FRAGMENTENTRY_WHERE, "",
			new FinderColumn<>(
				"fragmentEntry.", "externalReferenceCode",
				FinderColumn.Type.STRING, "=", true, true,
				FragmentEntry::getExternalReferenceCode),
			new FinderColumn<>(
				"fragmentEntry.", "groupId", FinderColumn.Type.LONG, "=", true,
				true, FragmentEntry::getGroupId));

		FragmentEntryUtil.setPersistence(this);
	}

	@Deactivate
	public void deactivate() {
		FragmentEntryUtil.setPersistence(null);

		entityCache.removeCache(FragmentEntryImpl.class.getName());
	}

	@Override
	@Reference(
		target = FragmentPersistenceConstants.SERVICE_CONFIGURATION_FILTER,
		unbind = "-"
	)
	public void setConfiguration(Configuration configuration) {
	}

	@Override
	@Reference(
		target = FragmentPersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Override
	@Reference(
		target = FragmentPersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	@Reference
	protected CTPersistenceHelper ctPersistenceHelper;

	@Reference
	protected EntityCache entityCache;

	@Reference
	protected FinderCache finderCache;

	private static final String _ENTITY_ALIAS_PREFIX =
		FragmentEntryModelImpl.ENTITY_ALIAS + ".";

	private static final String _SQL_SELECT_FRAGMENTENTRY =
		"SELECT fragmentEntry FROM FragmentEntry fragmentEntry";

	private static final String _SQL_SELECT_FRAGMENTENTRY_WHERE =
		"SELECT fragmentEntry FROM FragmentEntry fragmentEntry WHERE ";

	private static final String _SQL_COUNT_FRAGMENTENTRY_WHERE =
		"SELECT COUNT(fragmentEntry) FROM FragmentEntry fragmentEntry WHERE ";

	private static final Set<String> _badColumnNames = SetUtil.fromArray(
		new String[] {"uuid", "type"});

	@Override
	protected FinderCache getFinderCache() {
		return finderCache;
	}

}
// LIFERAY-SERVICE-BUILDER-HASH:11333584