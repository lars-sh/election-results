package de.larssh.election.germany.schleswigholstein;

import lombok.experimental.UtilityClass;

/**
 * This class contains helper methods to create unique keys.
 */
@UtilityClass
@SuppressWarnings("PMD.ShortClassName")
public class Keys {
	/**
	 * Any character to be used for escaping
	 */
	private static final String ESCAPE_CHARACTER = "\\";

	/**
	 * Creates a unique and still readable string out of {@code value} and
	 * {@code valueToEscape} by appending arguments in order and escaping
	 * {@code valueToEscape}.
	 *
	 * <p>
	 * <strong>Attention:</strong> If either {@code value} or {@code valueToEscape}
	 * is empty only the opposite value is returned. {@code valueToEscape} is still
	 * escaped, but to guarantee uniqueness {@code value} must not be empty.
	 *
	 * <p>
	 * {@code prefix} must be at least two characters long for the algorithm to work
	 * as expected. An {@link IllegalArgumentException} is thrown in case it is too
	 * short.
	 *
	 * <table style="width: 100%;">
	 * <caption>Real Examples</caption>
	 * <tr>
	 * <th>{@code value}
	 * <th>{@code prefix}
	 * <th>{@code valueToEscape}
	 * <th>{@code suffix}
	 * <th>return value
	 * </tr>
	 * <tr>
	 * <td>{@code "Knickrehm"}
	 * <td>{@code ", "}
	 * <td>{@code "Lars"}
	 * <td>{@code ""}
	 * <td>{@code "Knickrehm, Lars"}
	 * </tr>
	 * <tr>
	 * <td>{@code "Knickrehm, Lars"}
	 * <td>{@code " ("}
	 * <td>{@code "AWG"}
	 * <td>{@code ")"}
	 * <td>{@code "Knickrehm, Lars (AWG)"}
	 * </tr>
	 * </table>
	 *
	 * <table style="width: 100%;">
	 * <caption>Edge Cases</caption>
	 * <tr>
	 * <th>{@code value}
	 * <th>{@code prefix}
	 * <th>{@code valueToEscape}
	 * <th>{@code suffix}
	 * <th>return value
	 * </tr>
	 * <tr>
	 * <td>{@code ""}
	 * <td>{@code " ("}
	 * <td>{@code ""}
	 * <td>{@code ")"}
	 * <td>{@code ""}
	 * </tr>
	 * <tr>
	 * <td>{@code ""}
	 * <td>{@code " ("}
	 * <td>{@code "xyz"}
	 * <td>{@code ")"}
	 * <td>{@code "xyz"} (might not be unique)
	 * </tr>
	 * <tr>
	 * <td>{@code ""}
	 * <td>{@code " ("}
	 * <td>{@code "x (z"}
	 * <td>{@code ")"}
	 * <td>{@code "x \(z"} (might not be unique)
	 * </tr>
	 * <tr>
	 * <td>{@code "abc"}
	 * <td>{@code " ("}
	 * <td>{@code ""}
	 * <td>{@code ")"}
	 * <td>{@code "abc"}
	 * </tr>
	 * <tr>
	 * <td>{@code "abc"}
	 * <td>{@code " ("}
	 * <td>{@code "xyz"}
	 * <td>{@code ")"}
	 * <td>{@code "abc (xyz)"}
	 * </tr>
	 * <tr>
	 * <td>{@code "a (c"}
	 * <td>{@code " ("}
	 * <td>{@code ""}
	 * <td>{@code ")"}
	 * <td>{@code "a (c"}
	 * </tr>
	 * <tr>
	 * <td>{@code "a (c"}
	 * <td>{@code " ("}
	 * <td>{@code "x (z"}
	 * <td>{@code ")"}
	 * <td>{@code "a (c (x \(z)"}
	 * </tr>
	 * <tr>
	 * <td>{@code "a \(c"}
	 * <td>{@code " ("}
	 * <td>{@code "x \(z"}
	 * <td>{@code ")"}
	 * <td>{@code "a \(c (x \\(z)"}
	 * </tr>
	 * </table>
	 *
	 * @param value         the leading value
	 * @param prefix        the prefix of the value to escape
	 * @param valueToEscape the value to escape
	 * @param suffix        the suffix of the value to escape
	 * @return the unique value
	 */
	@SuppressWarnings("PMD.UseObjectForClearerAPI")
	public static String escape(final String value,
			final String prefix,
			final String valueToEscape,
			final String suffix) {
		if (valueToEscape.isEmpty()) {
			return value;
		}
		if (prefix.length() < 2) {
			throw new IllegalArgumentException("The prefix needs to contain at least two characters.");
		}

		final String escapedValue = valueToEscape.replace(ESCAPE_CHARACTER, ESCAPE_CHARACTER + ESCAPE_CHARACTER)
				.replace(Character.toString(prefix.charAt(0)) + Character.toString(prefix.charAt(1)),
						Character.toString(prefix.charAt(0)) + ESCAPE_CHARACTER + Character.toString(prefix.charAt(1)));
		return value.isEmpty() ? escapedValue : value + prefix + escapedValue + suffix;
	}
}
