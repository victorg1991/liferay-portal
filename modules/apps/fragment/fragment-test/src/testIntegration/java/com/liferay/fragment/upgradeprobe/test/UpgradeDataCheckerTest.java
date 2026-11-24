package com.liferay.fragment.upgradeprobe.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.conflict.ConflictInfo;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.change.tracking.service.CTProcessLocalService;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.FileInputStream;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@DataGuard(scope = DataGuard.Scope.NONE)
@RunWith(Arquillian.class)
public class UpgradeDataCheckerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testCheck() throws Exception {
		UserTestUtil.setUser(TestPropsValues.getUser());

		Properties properties = new Properties();

		try (FileInputStream fileInputStream = new FileInputStream(
				"/tmp/upg-data.properties")) {

			properties.load(fileInputStream);
		}

		for (String key :
				new String[] {"A", "B", "C", "D", "S1", "S2", "N"}) {

			_step(
				"rows " + key,
				() -> {
					long id = Long.parseLong(properties.getProperty(key + ".id"));

					FragmentEntry fragmentEntry =
						_fragmentEntryLocalService.fetchFragmentEntry(id);

					if (fragmentEntry == null) {
						return "MISSING";
					}

					FragmentEntry draft = _fragmentEntryLocalService.fetchDraft(
						fragmentEntry);

					return _describe(properties, key, fragmentEntry) +
						" | draft=" +
							(draft == null ? "none" :
								_describe(properties, key, draft));
				});
		}

		_step(
			"link D",
			() -> {
				FragmentEntry d = _fragmentEntryLocalService.getFragmentEntry(
					Long.parseLong(properties.getProperty("D.id")));

				List<FragmentEntryLink> links =
					_fragmentEntryLinkLocalService.
						getFragmentEntryLinksByFragmentEntry(
							d.getGroupId(), d);

				return "links=" + links.size() + " linkERCMatches=" +
					(links.size() == 1 &&
					 d.getExternalReferenceCode().equals(
						 links.get(0).getFragmentEntryERC()));
			});

		_step(
			"edit published with draft B",
			() -> {
				FragmentEntry b = _fragmentEntryLocalService.getFragmentEntry(
					Long.parseLong(properties.getProperty("B.id")));

				FragmentEntry draft = _fragmentEntryLocalService.getDraft(b);

				String draftHtml = draft.getHtml();

				draft.setHtml("<div>B-draft2</div>");

				_fragmentEntryLocalService.updateDraft(draft);

				FragmentEntry published = _fragmentEntryLocalService.publishDraft(
					draft);

				return "draftHtmlBefore=" + draftHtml + " publishedId=" +
					published.getFragmentEntryId() + " html=" +
						published.getHtml() + " erc=" +
							published.getExternalReferenceCode().equals(
								properties.getProperty("B.erc")) +
									" draftAfter=" +
										(_fragmentEntryLocalService.fetchDraft(
											published) == null ? "none" : "STILL");
			});

		_step(
			"publish never-published C",
			() -> {
				FragmentEntry c = _fragmentEntryLocalService.getFragmentEntry(
					Long.parseLong(properties.getProperty("C.id")));

				FragmentEntry published = _fragmentEntryLocalService.publishDraft(
					c);

				return "publishedId=" + published.getFragmentEntryId() +
					" head=" + published.isHead() + " erc=" +
						published.getExternalReferenceCode().equals(
							properties.getProperty("C.erc")) +
								" uuid=" +
									published.getUuid().equals(
										properties.getProperty("C.uuid"));
			});

		_step(
			"new draft from A then discard",
			() -> {
				FragmentEntry a = _fragmentEntryLocalService.getFragmentEntry(
					Long.parseLong(properties.getProperty("A.id")));

				FragmentEntry draft = _fragmentEntryLocalService.getDraft(a);

				String result =
					"draftIsHead=" + draft.isHead() + " draftErcDiffers=" +
						!draft.getExternalReferenceCode().equals(
							a.getExternalReferenceCode());

				_fragmentEntryLocalService.deleteFragmentEntry(draft);

				return result + " draftGone=" +
					(_fragmentEntryLocalService.fetchDraft(a) == null);
			});

		_step(
			"update published D through updateFragmentEntry",
			() -> {
				FragmentEntry d = _fragmentEntryLocalService.getFragmentEntry(
					Long.parseLong(properties.getProperty("D.id")));

				d.setHtml("<div>D-updated</div>");

				FragmentEntry updated =
					_fragmentEntryLocalService.updateFragmentEntry(d);

				return "html=" + updated.getHtml() + " head=" +
					updated.isHead();
			});

		_step(
			"delete D with link",
			() -> {
				try {
					_fragmentEntryLocalService.deleteFragmentEntry(
						Long.parseLong(properties.getProperty("D.id")));

					return "DELETED (unexpected)";
				}
				catch (Exception exception) {
					return "refused " + exception.getClass().getSimpleName();
				}
			});

		for (String ctKey : new String[] {"ctEditId", "ctAddId"}) {
			_step(
				"ct " + ctKey,
				() -> {
					CTCollection ctCollection =
						_ctCollectionLocalService.getCTCollection(
							Long.parseLong(properties.getProperty(ctKey)));

					Map<Long, List<ConflictInfo>> conflicts =
						_ctCollectionLocalService.checkConflicts(ctCollection);

					StringBuilder sb = new StringBuilder("conflicts=");

					for (Map.Entry<Long, List<ConflictInfo>> entry :
							conflicts.entrySet()) {

						for (ConflictInfo conflictInfo : entry.getValue()) {
							sb.append(
								conflictInfo.getConflictDescription(
									conflictInfo.getResourceBundle(
										java.util.Locale.US)) +
										"(resolved=" + conflictInfo.isResolved() +
											");");
						}
					}

					_ctProcessLocalService.addCTProcess(
						TestPropsValues.getUserId(),
						ctCollection.getCtCollectionId());

					ctCollection = _ctCollectionLocalService.getCTCollection(
						ctCollection.getCtCollectionId());

					return sb + " status=" + ctCollection.getStatus();
				});
		}

		for (String key : new String[] {"A", "B", "N"}) {
			_step(
				"after ct " + key,
				() -> {
					FragmentEntry fragmentEntry =
						_fragmentEntryLocalService.fetchFragmentEntry(
							Long.parseLong(properties.getProperty(key + ".id")));

					if (fragmentEntry == null) {
						return "MISSING";
					}

					FragmentEntry draft = _fragmentEntryLocalService.fetchDraft(
						fragmentEntry);

					return "headHtml=" + fragmentEntry.getHtml() +
						" draft=" +
							(draft == null ? "none" : draft.getHtml());
				});
		}
	}

	private String _describe(
		Properties properties, String key, FragmentEntry fragmentEntry) {

		return "id=" + fragmentEntry.getFragmentEntryId() + " head=" +
			fragmentEntry.isHead() + " headId=" + fragmentEntry.getHeadId() +
				" status=" + fragmentEntry.getStatus() + " ercSame=" +
					fragmentEntry.getExternalReferenceCode().equals(
						properties.getProperty(key + ".erc")) +
							" uuidSame=" +
								fragmentEntry.getUuid().equals(
									properties.getProperty(key + ".uuid")) +
										" html=" + fragmentEntry.getHtml();
	}

	private void _step(String label, Callable<String> callable) {
		String result = null;

		try {
			result = callable.call();
		}
		catch (Throwable throwable) {
			result = "EXCEPTION " + throwable;
		}

		System.out.println("UPG_CHECK " + label + ": " + result);
	}

	@Inject
	private CTCollectionLocalService _ctCollectionLocalService;

	@Inject
	private CTProcessLocalService _ctProcessLocalService;

	@Inject
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Inject
	private FragmentEntryLocalService _fragmentEntryLocalService;

}
