package net.sf.wubiq.android.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Converts between the milliseconds that are stored and the seconds that are shown.
 * @author Federico Alcantara
 *
 */
public final class SecondsUtils {

	private SecondsUtils() {
	}

	/**
	 * @param milliseconds Milliseconds.
	 * @param locale Locale used for the decimal separator.
	 * @return Seconds as text with at most one decimal, without trailing zeros (2500 is "2.5", 3000 is "3").
	 */
	public static String toSeconds(int milliseconds, Locale locale) {
		DecimalFormat format = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(locale));
		return format.format(milliseconds / 1000.0);
	}

	/**
	 * @param seconds Seconds as typed by the user; both "." and "," are accepted as decimal separator.
	 * @param defaultMilliseconds Value returned when the text is empty, negative or not a number.
	 * @param maxMilliseconds Upper bound of the returned value.
	 * @return Milliseconds in steps of 100 (seconds rounded to one decimal), between 0 and maxMilliseconds (larger values are lowered to maxMilliseconds).
	 */
	public static int toMilliseconds(String seconds, int defaultMilliseconds, int maxMilliseconds) {
		if (seconds == null) {
			return defaultMilliseconds;
		}
		try {
			double value = Double.parseDouble(seconds.trim().replace(',', '.'));
			if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) {
				return defaultMilliseconds;
			}
			return (int) Math.min(maxMilliseconds, Math.round(value * 10) * 100);
		} catch (NumberFormatException e) {
			return defaultMilliseconds;
		}
	}
}
