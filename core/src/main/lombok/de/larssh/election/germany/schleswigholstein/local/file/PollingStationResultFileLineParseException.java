package de.larssh.election.germany.schleswigholstein.local.file;

import de.larssh.utils.text.Strings;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;

/**
 * Thrown to indicate an errors while parsing {@link PollingStationResultFiles};
 * includes line information.
 */
public class PollingStationResultFileLineParseException extends RuntimeException {
	/**
	 * The error's line number
	 *
	 * @return the line number
	 */
	@Getter
	int lineNumber;

	/**
	 * The error's line content
	 *
	 * @return the line content
	 */
	@Getter
	String lineContent;

	/**
	 * Constructs a new {@link PollingStationResultFileLineParseException} with the
	 * given message, formatting as described at
	 * {@link Strings#format(String, Object...)}.
	 *
	 * @param lineNumber  the line number
	 * @param lineContent the line content
	 * @param message     the detail message
	 * @param arguments   arguments referenced by format specifiers in
	 *                    {@code message}
	 */
	public PollingStationResultFileLineParseException(final int lineNumber,
			final String lineContent,
			final String message,
			final Object... arguments) {
		this(null, lineNumber, lineContent, message, arguments);
	}

	/**
	 * Constructs a new {@link PollingStationResultFileLineParseException} with the
	 * given message, formatting as described at
	 * {@link Strings#format(String, Object...)}.
	 *
	 * @param throwable   the cause
	 * @param lineNumber  the line number
	 * @param lineContent the line content
	 * @param message     the detail message
	 * @param arguments   arguments referenced by format specifiers in
	 *                    {@code message}
	 */
	public PollingStationResultFileLineParseException(@Nullable final Throwable throwable,
			final int lineNumber,
			final String lineContent,
			final String message,
			final Object... arguments) {
		super(Strings.format(message, arguments), throwable);

		this.lineNumber = lineNumber;
		this.lineContent = lineContent;
	}
}
