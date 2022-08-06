package de.larssh.election.germany.schleswigholstein.local.cli;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.larssh.utils.Nullables;
import de.larssh.utils.function.IntToIntFunction;
import de.larssh.utils.text.Patterns;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

/**
 * A picocli compatible parameter set to specify either absolute or percentage
 * values.
 *
 * <p>
 * The created {@link IntToIntFunction} takes the total and calculates (in case
 * of a percentage value) the corresponding absolute value.
 */
@RequiredArgsConstructor
public class EitherAbsoluteOrPercentageTypeConverter implements ITypeConverter<IntToIntFunction> {
	/**
	 * One hundred
	 */
	private static final int HUNDRED = 100;

	/**
	 * Pattern to match either an absolute integer value or a percentage value.
	 * Values need to be zero or positive.
	 */
	private static final Pattern PATTERN = Pattern.compile("^\\s*(?<value>\\d+)(?<percentage>\\s*%)?\\s*$");

	/** {@inheritDoc} */
	@Override
	public IntToIntFunction convert(@Nullable final String value) {
		final Matcher matcher = Patterns.matches(PATTERN, Nullables.orElseThrow(value)) //
				.orElseThrow(() -> new TypeConversionException(String.format(
						"Invalid format. Expecting either a positive integer or a percentage value, but was '%s'.",
						value)));
		final int parsedValue = Integer.parseInt(matcher.group("value"));
		return matcher.group("percentage") == null //
				? total -> parsedValue
				: total -> (int) ((long) total * HUNDRED / parsedValue); // Use long to avoid integer overflows
	}
}
