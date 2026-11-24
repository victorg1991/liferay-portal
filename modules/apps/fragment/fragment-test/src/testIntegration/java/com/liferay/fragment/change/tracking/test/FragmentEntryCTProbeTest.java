/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.change.tracking.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.conflict.ConflictInfo;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.change.tracking.service.CTProcessLocalService;
import com.liferay.fragment.constants.FragmentConstants;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.service.FragmentCollectionLocalService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class FragmentEntryCTProbeTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testStaging() throws Exception {
		Group group = GroupTestUtil.addGroup();

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		com.liferay.portal.kernel.test.util.UserTestUtil.setUser(
			TestPropsValues.getUser());

		FragmentCollection fragmentCollection =
			_fragmentCollectionLocalService.addFragmentCollection(
				null, TestPropsValues.getUserId(), group.getGroupId(),
				RandomTestUtil.randomString(), StringPool.BLANK,
				serviceContext);

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.addFragmentEntry(
				null, TestPropsValues.getUserId(), group.getGroupId(),
				fragmentCollection.getFragmentCollectionId(), null,
				RandomTestUtil.randomString(), StringPool.BLANK, "<div>x</div>",
				StringPool.BLANK, false, null, null, 0, false, false,
				FragmentConstants.TYPE_COMPONENT, null,
				WorkflowConstants.STATUS_APPROVED, serviceContext);

		_logRows("STAGING before", fragmentEntry);

		try (com.liferay.portal.test.log.LogCapture logCapture =
				com.liferay.portal.test.log.LoggerTestUtil.configureLog4JLogger(
					"com.liferay.exportimport",
					com.liferay.portal.test.log.LoggerTestUtil.ERROR)) {

			_stagingLocalService.enableLocalStaging(
				TestPropsValues.getUserId(), group, false, false,
				serviceContext);

			for (com.liferay.portal.test.log.LogEntry logEntry :
					logCapture.getLogEntries()) {

				_log("STAGING", "log=" + logEntry.getMessage() + " " + logEntry.getThrowable());
			}
		}

		_logRows("STAGING after", fragmentEntry);
	}

	private void _logRows(String label, FragmentEntry fragmentEntry)
		throws Exception {

		for (FragmentEntry row :
				_fragmentEntryLocalService.getFragmentEntriesByUuidAndCompanyId(
					fragmentEntry.getUuid(), fragmentEntry.getCompanyId())) {

			_log(
				label,
				"row id=" + row.getFragmentEntryId() + " group=" +
					row.getGroupId() + " head=" + row.isHead() + " headId=" +
						row.getHeadId() + " status=" + row.getStatus());
		}
	}

	@Inject
	private com.liferay.exportimport.kernel.service.StagingLocalService
		_stagingLocalService;

	@Test
	public void testDraftDraft() throws Exception {
		_probe(
			"DRAFT_DRAFT", WorkflowConstants.STATUS_DRAFT,
			WorkflowConstants.STATUS_DRAFT);
	}

	@Test
	public void testDraftPublish() throws Exception {
		_probe(
			"DRAFT_PUBLISH", WorkflowConstants.STATUS_DRAFT,
			WorkflowConstants.STATUS_APPROVED);
	}

	@Test
	public void testPublishPublish() throws Exception {
		_probe(
			"PUBLISH_PUBLISH", WorkflowConstants.STATUS_APPROVED,
			WorkflowConstants.STATUS_APPROVED);
	}

	private void _edit(
			CTCollection ctCollection, FragmentEntry fragmentEntry,
			String html, int status)
		throws Exception {

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			_fragmentEntryLocalService.updateFragmentEntry(
					TestPropsValues.getUserId(),
					fragmentEntry.getFragmentEntryId(),
					fragmentEntry.getFragmentCollectionId(),
					fragmentEntry.getName(), StringPool.BLANK, html,
					StringPool.BLANK, false, null, null, 0, false, null,
					status);
		}
	}

	private String _draftHtml(FragmentEntry fragmentEntry) {
		FragmentEntry draftFragmentEntry = _fragmentEntryLocalService.fetchDraft(
			fragmentEntry);

		if (draftFragmentEntry == null) {
			return "none";
		}

		return draftFragmentEntry.getHtml() + "/status=" +
			draftFragmentEntry.getStatus();
	}

	private void _log(String scenario, String message) {
		System.out.println("CT_PROBE " + scenario + " " + message);
	}

	private void _logConflicts(
		String scenario, String label,
		Map<Long, List<ConflictInfo>> conflictsMap)
		throws Exception {

		if (conflictsMap.isEmpty()) {
			_log(scenario, label + " conflicts=none");

			return;
		}

		for (Map.Entry<Long, List<ConflictInfo>> entry :
				conflictsMap.entrySet()) {

			String className = _classNameLocalService.getClassName(
				entry.getKey()
			).getValue();

			for (ConflictInfo conflictInfo : entry.getValue()) {
				_log(
					scenario,
					label + " conflict class=" + className + " resolved=" +
						conflictInfo.isResolved() + " description=" +
							conflictInfo.getConflictDescription(
								conflictInfo.getResourceBundle(Locale.US)));
			}
		}
	}

	private void _probe(String scenario, int status1, int status2)
		throws Exception {

		Group group = GroupTestUtil.addGroup();

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		FragmentCollection fragmentCollection =
			_fragmentCollectionLocalService.addFragmentCollection(
				null, TestPropsValues.getUserId(), group.getGroupId(),
				RandomTestUtil.randomString(), StringPool.BLANK,
				serviceContext);

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.addFragmentEntry(
				null, TestPropsValues.getUserId(), group.getGroupId(),
				fragmentCollection.getFragmentCollectionId(), null,
				RandomTestUtil.randomString(), StringPool.BLANK, "html-0",
				StringPool.BLANK, false, null, null, 0, false, false,
				FragmentConstants.TYPE_COMPONENT, null,
				WorkflowConstants.STATUS_APPROVED, serviceContext);

		CTCollection ctCollection1 = _ctCollectionLocalService.addCTCollection(
			null, TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			0, "probe-1-" + RandomTestUtil.randomString(), null);
		CTCollection ctCollection2 = _ctCollectionLocalService.addCTCollection(
			null, TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			0, "probe-2-" + RandomTestUtil.randomString(), null);

		_edit(ctCollection1, fragmentEntry, "html-1", status1);
		_edit(ctCollection2, fragmentEntry, "html-2", status2);

		_logConflicts(
			scenario, "publication1",
			_ctCollectionLocalService.checkConflicts(ctCollection1));

		_ctProcessLocalService.addCTProcess(
			TestPropsValues.getUserId(), ctCollection1.getCtCollectionId());

		fragmentEntry = _fragmentEntryLocalService.getFragmentEntry(
			fragmentEntry.getFragmentEntryId());

		_log(scenario, "after publication1 html=" + fragmentEntry.getHtml() + " draft=" + _draftHtml(fragmentEntry));

		_logConflicts(
			scenario, "publication2",
			_ctCollectionLocalService.checkConflicts(ctCollection2));

		try {
			_ctProcessLocalService.addCTProcess(
				TestPropsValues.getUserId(),
				ctCollection2.getCtCollectionId());
		}
		catch (Exception exception) {
			_log(scenario, "publication2 publish exception=" + exception);
		}

		ctCollection2 = _ctCollectionLocalService.getCTCollection(
			ctCollection2.getCtCollectionId());

		_log(
			scenario,
			"publication2 collection status=" + ctCollection2.getStatus());

		fragmentEntry = _fragmentEntryLocalService.getFragmentEntry(
			fragmentEntry.getFragmentEntryId());

		_log(
			scenario,
			"final head html=" + fragmentEntry.getHtml() + " status=" +
				fragmentEntry.getStatus() + " draft=" +
					_draftHtml(fragmentEntry));
	}

	@Inject
	private ClassNameLocalService _classNameLocalService;

	@Inject
	private CTCollectionLocalService _ctCollectionLocalService;

	@Inject
	private CTProcessLocalService _ctProcessLocalService;

	@Inject
	private FragmentCollectionLocalService _fragmentCollectionLocalService;

	@Inject
	private FragmentEntryLocalService _fragmentEntryLocalService;

}
