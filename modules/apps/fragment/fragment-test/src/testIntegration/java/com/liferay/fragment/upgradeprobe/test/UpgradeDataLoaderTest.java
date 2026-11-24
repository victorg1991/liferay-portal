package com.liferay.fragment.upgradeprobe.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.exportimport.kernel.service.StagingLocalService;
import com.liferay.fragment.constants.FragmentConstants;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.fragment.test.util.FragmentTestUtil;
import com.liferay.layout.test.util.ContentLayoutTestUtil;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ScopeUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.segments.service.SegmentsExperienceLocalService;

import java.io.FileOutputStream;
import java.util.Properties;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@DataGuard(scope = DataGuard.Scope.NONE)
@RunWith(Arquillian.class)
public class UpgradeDataLoaderTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testLoad() throws Exception {
		UserTestUtil.setUser(TestPropsValues.getUser());

		Properties properties = new Properties();

		Group group = GroupTestUtil.addGroup();

		properties.setProperty("groupId", String.valueOf(group.getGroupId()));

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		FragmentCollection fragmentCollection =
			FragmentTestUtil.addFragmentCollection(group.getGroupId());

		properties.setProperty(
			"collectionId",
			String.valueOf(fragmentCollection.getFragmentCollectionId()));

		FragmentEntry a = _add(
			fragmentCollection, "UPG-A", "erc-A-" + RandomTestUtil.randomString(8),
			WorkflowConstants.STATUS_APPROVED, "<div>A</div>", serviceContext);

		_record(properties, "A", a);

		FragmentEntry b = _add(
			fragmentCollection, "UPG-B", "erc-B-" + RandomTestUtil.randomString(8),
			WorkflowConstants.STATUS_APPROVED, "<div>B</div>", serviceContext);

		FragmentEntry bDraft = _fragmentEntryLocalService.getDraft(b);

		bDraft.setHtml("<div>B-draft</div>");

		_fragmentEntryLocalService.updateDraft(bDraft);

		_record(properties, "B", b);

		FragmentEntry c = _add(
			fragmentCollection, "UPG-C", "erc-C-" + RandomTestUtil.randomString(8),
			WorkflowConstants.STATUS_DRAFT, "<div>C-draft</div>",
			serviceContext);

		_record(properties, "C", c);

		FragmentEntry d = _add(
			fragmentCollection, "UPG-D", "erc-D-" + RandomTestUtil.randomString(8),
			WorkflowConstants.STATUS_APPROVED, "<div>D</div>", serviceContext);

		FragmentEntry dDraft = _fragmentEntryLocalService.getDraft(d);

		dDraft.setHtml("<div>D-draft</div>");

		_fragmentEntryLocalService.updateDraft(dDraft);

		_record(properties, "D", d);

		Layout layout = LayoutTestUtil.addTypeContentPublishedLayout(
			group, RandomTestUtil.randomString(),
			WorkflowConstants.STATUS_APPROVED);

		Layout draftLayout = layout.fetchDraftLayout();

		long segmentsExperienceId =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				draftLayout.getPlid());

		FragmentEntryLink fragmentEntryLink =
			ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
				null, d.getCss(), d.getConfiguration(),
				d.getExternalReferenceCode(),
				ScopeUtil.getItemScopeExternalReferenceCode(
					d.getGroupId(), draftLayout.getGroupId()),
				d.getHtml(), d.getJs(), draftLayout, d.getFragmentEntryKey(),
				d.getType(), null, 0, segmentsExperienceId);

		ContentLayoutTestUtil.publishLayout(draftLayout, layout);

		properties.setProperty(
			"linkERC", fragmentEntryLink.getExternalReferenceCode());
		properties.setProperty("plid", String.valueOf(layout.getPlid()));

		Group stagedGroup = GroupTestUtil.addGroup();

		ServiceContext stagedServiceContext =
			ServiceContextTestUtil.getServiceContext(stagedGroup.getGroupId());

		FragmentCollection stagedCollection =
			FragmentTestUtil.addFragmentCollection(stagedGroup.getGroupId());

		FragmentEntry s1 = _add(
			stagedCollection, "UPG-S1",
			"erc-S1-" + RandomTestUtil.randomString(8),
			WorkflowConstants.STATUS_APPROVED, "<div>S1</div>",
			stagedServiceContext);

		FragmentEntry s2 = _add(
			stagedCollection, "UPG-S2",
			"erc-S2-" + RandomTestUtil.randomString(8),
			WorkflowConstants.STATUS_APPROVED, "<div>S2</div>",
			stagedServiceContext);

		FragmentEntry s2Draft = _fragmentEntryLocalService.getDraft(s2);

		s2Draft.setHtml("<div>S2-draft</div>");

		_fragmentEntryLocalService.updateDraft(s2Draft);

		_stagingLocalService.enableLocalStaging(
			TestPropsValues.getUserId(), stagedGroup, false, false,
			stagedServiceContext);

		properties.setProperty(
			"stagedGroupId", String.valueOf(stagedGroup.getGroupId()));
		properties.setProperty(
			"stagingGroupId",
			String.valueOf(stagedGroup.getStagingGroup().getGroupId()));

		_record(properties, "S1", s1);
		_record(properties, "S2", s2);

		CTCollection ctCollection1 = _ctCollectionLocalService.addCTCollection(
			null, TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			0, "UPG-ct-edit", null);

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection1.getCtCollectionId())) {

			FragmentEntry ctDraft = _fragmentEntryLocalService.getDraft(
				_fragmentEntryLocalService.getFragmentEntry(
					a.getFragmentEntryId()));

			ctDraft.setHtml("<div>A-ct-draft</div>");

			_fragmentEntryLocalService.updateDraft(ctDraft);

			FragmentEntry ctBHead = _fragmentEntryLocalService.getFragmentEntry(
				b.getFragmentEntryId());

			ctBHead.setHtml("<div>B-ct-published</div>");

			_fragmentEntryLocalService.updateFragmentEntry(ctBHead);
		}

		properties.setProperty(
			"ctEditId", String.valueOf(ctCollection1.getCtCollectionId()));

		CTCollection ctCollection2 = _ctCollectionLocalService.addCTCollection(
			null, TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			0, "UPG-ct-add", null);

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection2.getCtCollectionId())) {

			FragmentEntry n = _add(
				fragmentCollection, "UPG-N",
				"erc-N-" + RandomTestUtil.randomString(8),
				WorkflowConstants.STATUS_APPROVED, "<div>N</div>",
				serviceContext);

			FragmentEntry nDraft = _fragmentEntryLocalService.getDraft(n);

			nDraft.setHtml("<div>N-draft</div>");

			_fragmentEntryLocalService.updateDraft(nDraft);

			_record(properties, "N", n);
		}

		properties.setProperty(
			"ctAddId", String.valueOf(ctCollection2.getCtCollectionId()));

		try (FileOutputStream fileOutputStream = new FileOutputStream(
				"/tmp/upg-data.properties")) {

			properties.store(fileOutputStream, null);
		}
	}

	private FragmentEntry _add(
			FragmentCollection fragmentCollection, String name, String erc,
			int status, String html, ServiceContext serviceContext)
		throws Exception {

		return _fragmentEntryLocalService.addFragmentEntry(
			erc, TestPropsValues.getUserId(), serviceContext.getScopeGroupId(),
			fragmentCollection.getFragmentCollectionId(), null, name,
			StringPool.BLANK, html, StringPool.BLANK, false, null, null, 0,
			false, false, FragmentConstants.TYPE_COMPONENT, null, status,
			serviceContext);
	}

	private void _record(
		Properties properties, String key, FragmentEntry fragmentEntry) {

		properties.setProperty(
			key + ".id", String.valueOf(fragmentEntry.getFragmentEntryId()));
		properties.setProperty(
			key + ".erc", fragmentEntry.getExternalReferenceCode());
		properties.setProperty(key + ".uuid", fragmentEntry.getUuid());
	}

	@Inject
	private CTCollectionLocalService _ctCollectionLocalService;

	@Inject
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	@Inject
	private StagingLocalService _stagingLocalService;

}
