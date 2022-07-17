package de.larssh.election.germany.schleswigholstein.local.file;

import de.larssh.election.germany.schleswigholstein.ElectionException;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;

@Getter
public class PollingStationResultFileLineParseException extends ElectionException {
	int lineNumber;

	String lineContent;

	public PollingStationResultFileLineParseException(final String message,
			final int lineNumber,
			final String lineContent) {
		this(null, message, lineNumber, lineContent);
	}

	public PollingStationResultFileLineParseException(@Nullable final Throwable throwable,
			final String message,
			final int lineNumber,
			final String lineContent) {
		super(message, throwable);

		this.lineNumber = lineNumber;
		this.lineContent = lineContent;
	}
}
