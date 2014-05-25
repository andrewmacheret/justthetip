package com.justthetip.util;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.justthetip.exception.ValidationException;

public class Currency {
	private static final Pattern CURRENCY_PATTERN = Pattern.compile("([+-]?)([0-9]+)(\\.([0-9]{0,2}))?");
	private static final int CURRENCY_SIGN_GROUP = 1;
	private static final int CURRENCY_NUMBER_GROUP = 2;
	private static final int CURRENCY_DECIMAL_GROUP = 4;

	/**
	 * Parse cents from a string in the form "XXX.YY". May or may not include decimal and sign. Must not include $.
	 * 
	 * @param text
	 *            The string to parse.
	 * @return The number of cents.
	 * @throws ParseException
	 */
	public static int parseCurrency(CharSequence text) throws ValidationException {
		if (text == null) throw new ValidationException("Please input a cost.");
		Matcher match = CURRENCY_PATTERN.matcher(text);
		if (!match.matches()) {
			throw new ValidationException("Please input a cost in the format 'XXX.YY': " + text);
		}
		
		boolean positive = !"-".equals(match.group(CURRENCY_SIGN_GROUP));
		int number = Integer.parseInt(match.group(CURRENCY_NUMBER_GROUP));
		
		String decimalString = match.group(CURRENCY_DECIMAL_GROUP);
		int decimal = decimalString != null && decimalString.length() != 0 ? Integer.parseInt(decimalString) : 0;
		
		return (positive ? 1 : -1) * (100 * number + decimal);
	}

	/**
	 * Format cents to a string in the form "XXX.YY". Will not include $.
	 * 
	 * @param cents
	 *            Number of cents.
	 * @return The string representation.
	 */
	public static String formatCurrency(int cents) {
		int absCents;
		String sign;
		if (cents >= 0) {
			absCents = cents;
			sign = "";
		} else {
			absCents = -cents;
			sign = "-";
		}
		int decimal = absCents % 100;
		String decimalString = (decimal < 10 ? "0" : "") + decimal;
		return sign + (absCents / 100) + "." + decimalString;
	}
}
