package de.larssh.election.germany.schleswigholstein.local.file;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;

import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

/**
 * Thrown to indicate one or more errors while parsing
 * {@link PollingStationResultFiles}.
 *
 * <p>
 * An election result excluding lines with parse errors can be obtained by
 * {@link #getIncompleteResult()}. Suppressed errors with line information can
 * be obtained by {@code #getSuppressedLineParseExceptions()}.
 */
public class PollingStationResultFileParseException extends ElectionException {
	/**
	 * An election result excluding lines with parse errors.
	 *
	 * @return a probably incomplete election result
	 */
	@Getter
	LocalElectionResult incompleteResult;

	/**
	 * Constructs a new {@link PollingStationResultFileParseException} with the
	 * given message, formatting as described at
	 * {@link de.larssh.utils.text.Strings#format(String, Object...)}.
	 *
	 * @param suppressed       the suppressed errors with line information
	 * @param incompleteResult the probably incomplete election result
	 * @param message          the detail message
	 * @param arguments        arguments referenced by format specifiers in
	 *                         {@code message}
	 */
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Objects are as less mutuable as possible")
	public PollingStationResultFileParseException(
			final Collection<PollingStationResultFileLineParseException> suppressed,
			final LocalElectionResult incompleteResult,
			final String message,
			final Object... arguments) {
		super(message, arguments);

		this.incompleteResult = incompleteResult;
		suppressed.forEach(this::addSuppressed);
	}

	/**
	 * Casts {@link #getSuppressed()} to a list of errors with line information.
	 *
	 * @return the suppressed errors with line information
	 */
	public Collection<PollingStationResultFileLineParseException> getSuppressedLineParseExceptions() {
		return Arrays.stream(getSuppressed())
				.map(PollingStationResultFileLineParseException.class::cast)
				.collect(toList());
	}
}
