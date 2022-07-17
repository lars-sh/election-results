package de.larssh.election.germany.schleswigholstein.local.file;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;

import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import lombok.Getter;

@Getter
public class PollingStationResultFileParseException extends ElectionException {
	private LocalElectionResult incompleteResult;

	public PollingStationResultFileParseException(final Collection<Throwable> suppressed,
			final LocalElectionResult incompleteResult,
			final String message,
			final Object... arguments) {
		super(message, arguments);

		suppressed.forEach(this::addSuppressed);
		this.incompleteResult = incompleteResult;
	}

	public Collection<PollingStationResultFileLineParseException> getSuppressedLineParseExceptions() {
		return Arrays.stream(getSuppressed()).map(e -> {
			if (!(e instanceof PollingStationResultFileLineParseException)) {
				throw new IllegalArgumentException(
						"Failed casting suppressed exception to PollingStationResultFileLineParseException.",
						e);
			}
			return (PollingStationResultFileLineParseException) e;
		}).collect(toList());
	}
}
