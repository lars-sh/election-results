package de.larssh.election.germany.schleswigholstein;

import de.larssh.utils.text.Strings;

/**
 * Thrown to indicate election data inconsistencies.
 */
public class ElectionException extends RuntimeException {
	/**
	 * Constructs a new {@link ElectionException} with the given message, formatting
	 * as described at {@link Strings#format(String, Object...)}.
	 *
	 * @param message   the detail message
	 * @param arguments arguments referenced by format specifiers in {@code message}
	 */
	public ElectionException(final String message, final Object... arguments) {
		super(Strings.format(message, arguments), null);
	}
}
