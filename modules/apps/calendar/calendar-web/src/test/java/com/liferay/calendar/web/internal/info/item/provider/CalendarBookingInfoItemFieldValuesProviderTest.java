/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.calendar.web.internal.info.item.provider;

import com.liferay.calendar.model.CalendarBooking;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Carolina Barbosa
 */
public class CalendarBookingInfoItemFieldValuesProviderTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_setUpCalendarBooking();
	}

	@Test
	public void testGetCalendarBookingURL() throws Exception {
		ServiceContext serviceContext = Mockito.mock(ServiceContext.class);

		ThemeDisplay themeDisplay = Mockito.mock(ThemeDisplay.class);

		Mockito.when(
			themeDisplay.getPathFriendlyURLPublic()
		).thenReturn(
			_PATH_FRIENDLY_URL_PUBLIC
		);

		Mockito.when(
			themeDisplay.getPortalURL()
		).thenReturn(
			_PORTAL_URL
		);

		Mockito.when(
			serviceContext.getThemeDisplay()
		).thenReturn(
			themeDisplay
		);

		try {
			ServiceContextThreadLocal.pushServiceContext(serviceContext);

			Assert.assertEquals(
				StringBundler.concat(
					_PORTAL_URL, _PATH_FRIENDLY_URL_PUBLIC,
					"/calendar/shared/-/calendar/", _CALENDAR_BOOKING_ID),
				_calendarBookingInfoItemFieldValuesProvider.
					getCalendarBookingURL(_calendarBooking));
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}
	}

	private void _setUpCalendarBooking() throws Exception {
		Mockito.when(
			_calendarBooking.getCalendarBookingId()
		).thenReturn(
			_CALENDAR_BOOKING_ID
		);
	}

	private static final long _CALENDAR_BOOKING_ID =
		RandomTestUtil.randomLong();

	private static final String _PATH_FRIENDLY_URL_PUBLIC =
		RandomTestUtil.randomString();

	private static final String _PORTAL_URL = RandomTestUtil.randomString();

	private final CalendarBooking _calendarBooking = Mockito.mock(
		CalendarBooking.class);
	private final CalendarBookingInfoItemFieldValuesProvider
		_calendarBookingInfoItemFieldValuesProvider =
			new CalendarBookingInfoItemFieldValuesProvider();

}