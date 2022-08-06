package de.larssh.election.germany.schleswigholstein.local.cli;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.larssh.utils.Nullables;
import de.larssh.utils.function.DoubleToDoubleFunction;
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
 * The created {@link DoubleToDoubleFunction} takes the total and calculates (in
 * case of a percentage value) the corresponding absolute value.
 */
@RequiredArgsConstructor
public class EitherAbsoluteOrPercentageTypeConverter implements ITypeConverter<DoubleToDoubleFunction> {
	/**
	 * One hundred
	 */
	private static final int HUNDRED = 100;

	/**
	 * Pattern to match either an absolute double value or a percentage value.
	 * Values need to be zero or positive.
	 */
	private static final Pattern PATTERN = Pattern.compile("^\\s*(?<value>\\d+(\\.\\d*)?)(?<percentage>\\s*%)?\\s*$");

	/** {@inheritDoc} */
	@Override
	public DoubleToDoubleFunction convert(@Nullable final String value) {
		final Matcher matcher = Patterns.matches(PATTERN, Nullables.orElseThrow(value))
				.orElseThrow(() -> new TypeConversionException(String.format(
						"Invalid format. Expecting either a positive double or a percentage value, but was \"%s\".",
						value)));
		final double parsedValue = Double.parseDouble(matcher.group("value"));
		return matcher.group("percentage") == null //
				? total -> parsedValue
				: total -> parsedValue * total / HUNDRED;
	}
}
