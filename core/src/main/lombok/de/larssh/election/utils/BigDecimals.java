package de.larssh.election.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import lombok.experimental.UtilityClass;

/**
 * This class contains helper methods for {@link BigDecimal}.
 */
@UtilityClass
public class BigDecimals {
	/**
	 * Calculates {@code dividend / divisor} at {@code scale} and with
	 * {@link RoundingMode#HALF_UP}.
	 *
	 * @param dividend the value to be divided
	 * @param divisor  the value to divide with
	 * @param scale    the scale of the quotient to be returned
	 * @return the result as {@link BigDecimal}
	 */
	public static BigDecimal divide(final long dividend, final long divisor, final int scale) {
		return divide(BigDecimal.valueOf(dividend), divisor, scale);
	}

	/**
	 * Calculates {@code dividend / divisor} at {@code scale} and with
	 * {@link RoundingMode#HALF_UP}.
	 *
	 * @param dividend the value to be divided
	 * @param divisor  the value to divide with
	 * @param scale    the scale of the quotient to be returned
	 * @return the result as {@link BigDecimal}
	 */
	public static BigDecimal divide(final BigDecimal dividend, final long divisor, final int scale) {
		return divide(dividend, BigDecimal.valueOf(divisor), scale);
	}

	/**
	 * Calculates {@code dividend / divisor} at {@code scale} and with
	 * {@link RoundingMode#HALF_UP}.
	 *
	 * @param dividend the value to be divided
	 * @param divisor  the value to divide with
	 * @param scale    the scale of the quotient to be returned
	 * @return the result as {@link BigDecimal}
	 */
	public static BigDecimal divide(final long dividend, final BigDecimal divisor, final int scale) {
		return divide(BigDecimal.valueOf(dividend), divisor, scale);
	}

	/**
	 * Calculates {@code dividend / divisor} at {@code scale} and with
	 * {@link RoundingMode#HALF_UP}.
	 *
	 * @param dividend the value to be divided
	 * @param divisor  the value to divide with
	 * @param scale    the scale of the quotient to be returned
	 * @return the result as {@link BigDecimal}
	 */
	private static BigDecimal divide(final BigDecimal dividend, final BigDecimal divisor, final int scale) {
		return dividend.divide(divisor, scale, RoundingMode.HALF_UP);
	}

	/**
	 * Calculates {@code dividend / divisor} at {@code scale} and with
	 * {@link RoundingMode#HALF_UP}. If {@code divisor} is zero this method does
	 * <b>not</b> throw {@link ArithmeticException}, but returns zero.
	 *
	 * @param dividend the value to be divided
	 * @param divisor  the value to divide with
	 * @param scale    the scale of the quotient to be returned
	 * @return zero if {@code divisor} is zero or the result as {@link BigDecimal}
	 */
	public static BigDecimal divideOrZero(final long dividend, final long divisor, final int scale) {
		if (divisor == 0) {
			return BigDecimal.ZERO;
		}
		return divide(dividend, divisor, scale);
	}

	/**
	 * Formats {@code value} according to {@code locale} and using the precision
	 * specified by {@code scale}.
	 *
	 * @param value  the value
	 * @param scale  the precision to use for formatting
	 * @param locale the locale to apply
	 * @return the formatted value
	 */
	public static String format(final BigDecimal value, final int scale, final Locale locale) {
		return String.format(locale, "%." + Integer.toString(scale) + "f", value);
	}

	/**
	 * Determines if {@code value} is zero.
	 *
	 * @param value the value to check
	 * @return {@code true} if {@code value} is zero, else {@code false}
	 */
	public static boolean isZero(final BigDecimal value) {
		return value.compareTo(BigDecimal.ZERO) == 0;
	}
}
