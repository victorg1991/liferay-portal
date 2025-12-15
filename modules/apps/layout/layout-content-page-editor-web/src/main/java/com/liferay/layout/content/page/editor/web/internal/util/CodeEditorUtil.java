package com.liferay.layout.content.page.editor.web.internal.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.http.HttpServletRequest;

public class CodeEditorUtil {

	public static JSONObject buildFieldsSection(
		Language language, HttpServletRequest httpServletRequest) {

		return JSONUtil.put(
			"items",
			JSONUtil.putAll(
				_createSidebarElement(
					language, httpServletRequest, "createDate",
					StringPool.BLANK, "create-date"),
				_createSidebarElement(
					language, httpServletRequest, "emailAddresses",
					StringPool.BLANK, "email-addresses"),
				_createSidebarElement(
					language, httpServletRequest, "lastLoginDate",
					StringPool.BLANK, "last-login-date"),
				_createSidebarElement(
					language, httpServletRequest, "modifiedDate",
					StringPool.BLANK, "modified-date"),
				_createSidebarElement(
					language, httpServletRequest, "screenName",
					StringPool.BLANK, "screen-name"),
				_createSidebarElement(
					language, httpServletRequest, "userId", StringPool.BLANK,
					"user-id"))
		).put(
			"label", language.get(httpServletRequest, "fields")
		);
	}

	public static JSONObject buildFunctionsSection(
			Language language, HttpServletRequest httpServletRequest)
		throws Exception {

		return JSONUtil.put(
			"items",
			JSONUtil.toJSONArray(
				DDMExpressionFunction.values(),
				ddmExpressionFunction -> _createSidebarElement(
					language, httpServletRequest,
					ddmExpressionFunction._content,
					ddmExpressionFunction._helpTextKey,
					ddmExpressionFunction._key))
		).put(
			"label", language.get(httpServletRequest, "functions")
		);
	}

	public static JSONObject buildGeneralVariablesSection(
		Language language, HttpServletRequest httpServletRequest) {

		return JSONUtil.put(
			"items",
			JSONUtil.putAll(
				_createSidebarElement(
					language, httpServletRequest, "currentDate",
					StringPool.BLANK, "current-date"),
				_createSidebarElement(
					language, httpServletRequest, "roleIds", StringPool.BLANK,
					"role-ids"),
				_createSidebarElement(
					language, httpServletRequest, "segmentsEntryIds",
					StringPool.BLANK, "segments-entry-ids"))
		).put(
			"label", language.get(httpServletRequest, "general-variables")
		);
	}

	public static JSONObject buildOperatorsSection(
			Language language, HttpServletRequest httpServletRequest)
		throws Exception {

		return JSONUtil.put(
			"items",
			JSONUtil.toJSONArray(
				DDMExpressionOperator.values(),
				ddmExpressionOperator -> _createSidebarElement(
					language, httpServletRequest,
					ddmExpressionOperator._content,
					ddmExpressionOperator._helpTextKey,
					ddmExpressionOperator._key))
		).put(
			"label", language.get(httpServletRequest, "operators")
		);
	}

	public static JSONArray buildSidebarSections(
			Language language, HttpServletRequest httpServletRequest)
		throws Exception {

		return JSONUtil.putAll(
			buildFieldsSection(language, httpServletRequest),
			buildGeneralVariablesSection(language, httpServletRequest),
			buildOperatorsSection(language, httpServletRequest),
			buildFunctionsSection(language, httpServletRequest));
	}

	private static JSONObject _createSidebarElement(
		Language language, HttpServletRequest httpServletRequest,
		String content, String helpTextKey, String labelKey) {

		return JSONUtil.put(
			"content", content
		).put(
			"helpText",
			() -> {
				if (Validator.isNotNull(helpTextKey)) {
					return language.get(httpServletRequest, helpTextKey);
				}

				return null;
			}
		).put(
			"label", language.get(httpServletRequest, labelKey)
		);
	}

	public enum DDMExpressionFunction {

		ADD_DAYS(
			"addDays(field_name, parameter)",
			"calculates-the-result-of-adding-or-subtracting-a-specified-" +
				"number-of-days-from-a-given-date.-to-subtract,-prefix-the-" +
					"number-of-days-with-a-minus-sign",
			"add-days"),
		ADD_MONTHS(
			"addMonths(field_name, parameter)",
			"calculates-the-result-of-adding-or-subtracting-a-specified-" +
				"number-of-months-from-a-given-date.-to-subtract,-prefix-the-" +
					"number-of-months-with-a-minus-sign",
			"add-months"),
		ADD_YEARS(
			"addYears(field_name, parameter)",
			"calculates-the-result-of-adding-or-subtracting-a-specified-" +
				"number-of-years-from-a-given-date.-to-subtract,-prefix-the-" +
					"number-of-years-with-a-minus-sign",
			"add-years"),
		COMPARE_DATES(
			"compareDates(field_name, parameter)",
			"check-if-a-field-has-the-same-date-of-the-value", "compare-dates"),
		CONCAT(
			"concat(parameter1, parameter2, parameterN)",
			"combine-multiple-strings-or-text-fields-and-return-a-single-" +
				"string-that-can-be-used-with-other-validation-functions",
			"concat"),
		CONDITION(
			"condition(condition, parameter1, parameter2)",
			"provide-for-the-customer-the-possibility-of-condition-for-" +
				"values-or-fields-and-determines-if-expressions-are-true-or-" +
					"false",
			"condition"),
		CONTAINS(
			"contains(field_name, parameter)",
			"check-if-a-field-contains-a-specific-value-and-return-a-boolean",
			"contains"),
		DOES_NOT_CONTAIN(
			"NOT(contains(field_name, parameter))",
			"check-if-a-field-contains-a-specific-value-and-return-a-boolean-" +
				"if-the-field-does-contain-the-value-it-is-invalid",
			"does-not-contain"),
		FUTURE_DATES(
			"futureDates(field_name, parameter)",
			"check-if-a-date-fields-value-is-in-the-future-and-return-a-" +
				"boolean",
			"future-dates"),
		IS_A_URL(
			"isURL(field_name)",
			"check-if-a-text-field-is-a-URL-and-return-a-boolean", "is-a-url"),
		IS_AN_EMAIL(
			"isEmailAddress(field_name)",
			"check-if-a-text-field-is-an-email-and-return-a-boolean",
			"is-an-email"),
		IS_DECIMAL(
			"isDecimal(parameter)",
			"check-if-a-numeric-field-is-a-decimal-and-return-a-boolean",
			"is-decimal"),
		IS_EMPTY(
			"isEmpty(parameter)",
			"check-if-a-text-field-is-empty-and-return-a-boolean", "is-empty"),
		IS_EQUAL_TO(
			"field_name == parameter",
			"check-if-a-field-is-equal-to-a-specific-value-and-return-a-" +
				"boolean",
			"is-equal-to"),
		IS_GREATER_THAN(
			"field_name > parameter",
			"check-if-a-numeric-field-is-greater-than-a-specific-numeric-" +
				"value-and-return-a-boolean",
			"is-greater-than"),
		IS_GREATER_THAN_OR_EQUAL_TO(
			"field_name >= parameter",
			"check-if-a-numeric-field-is-greater-than-or-equal-to-a-specific-" +
				"numeric-value-and-return-a-boolean",
			"is-greater-than-or-equal-to"),
		IS_INTEGER(
			"isInteger(parameter)",
			"check-if-a-numeric-field-is-an-integer-and-return-a-boolean",
			"is-integer"),
		IS_LESS_THAN(
			"field_name < parameter",
			"check-if-a-numeric-field-is-less-than-a-specific-numeric-value-" +
				"and-return-a-boolean",
			"is-less-than"),
		IS_LESS_THAN_OR_EQUAL_TO(
			"field_name <= parameter",
			"check-if-a-numeric-field-is-less-than-or-equal-to-a-specific-" +
				"numeric-value-and-return-a-boolean",
			"is-less-than-or-equal-to"),
		IS_NOT_EQUAL_TO(
			"field_name != parameter",
			"check-if-a-field-is-not-equal-to-a-specific-value-and-return-a-" +
				"boolean",
			"is-not-equal-to"),
		MATCH(
			"match(field_name, parameter)",
			"check-if-a-text-field-matches-a-specific-string-value-or-regex-" +
				"expression-and-return-a-boolean",
			"match"),
		OLD_VALUE(
			"oldValue(\"field_name\")",
			"use-the-previous-value-of-a-field-before-its-update-to-create-" +
				"more-accurate-conditions",
			"old-value"),
		PAST_DATES(
			"pastDates(field_name, parameter)",
			"check-if-a-date-fields-value-is-in-the-past-and-return-a-boolean",
			"past-dates"),
		POW(
			"pow(field_name, parameter)",
			"raise-a-number-to-a-power-of-a-specified-number", "power"),
		RANGE(
			"futureDates(field_name, parameter) AND pastDates(" +
				"field_name, parameter)",
			"check-if-a-date-range-begins-with-a-past-date-and-ends-with-a-" +
				"future-date",
			"range"),
		SUM(
			"sum(parameter1, parameter2, parameterN)",
			"add-multiple-numeric-fields-together-and-return-a-single-number-" +
				"that-can-be-used-with-other-validation-functions",
			"sum");

		private DDMExpressionFunction(
			String content, String helpTextKey, String key) {

			_content = content;
			_helpTextKey = helpTextKey;
			_key = key;
		}

		private String _content;
		private String _helpTextKey;
		private String _key;

	}

	public enum DDMExpressionOperator {

		AND(
			"AND",
			"this-is-a-type-of-coordinating-conjunction-that-is-commonly-" +
				"used-to-indicate-a-dependent-relationship",
			"and"),
		DIVIDED_BY(
			"field_name1 / field_name2",
			"divide-one-numeric-field-by-another-to-create-an-expression",
			"divided-by"),
		MINUS(
			"field_name1 - field_name2",
			"subtract-numeric-fields-from-one-another-to-create-an-expression",
			"minus"),
		OR(
			"OR",
			"this-is-a-type-of-coordinating-conjunction-that-indicates-an-" +
				"independent-relationship",
			"or"),
		PLUS(
			"field_name1 + field_name2",
			"add-numeric-fields-to-create-an-expression", "plus"),
		TIMES(
			"field_name1 * field_name2",
			"multiply-numeric-fields-to-create-an-expression", "times");

		DDMExpressionOperator(String content, String helpTextKey, String key) {
			_content = content;
			_helpTextKey = helpTextKey;
			_key = key;
		}

		private final String _content;
		private final String _helpTextKey;
		private final String _key;

	}

}