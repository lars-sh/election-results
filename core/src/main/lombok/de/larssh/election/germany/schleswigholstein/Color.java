package de.larssh.election.germany.schleswigholstein;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import de.larssh.utils.Nullables;
import de.larssh.utils.text.Patterns;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Color following the RGBA color model and storing each channel in percentages
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class Color {
	/**
	 * Pattern to parse colors in hexadecimal form
	 */
	private static final Pattern HEX_PATTERN
			= Pattern.compile("^#?(?<red>[0-9a-f]{2})(?<green>[0-9a-f]{2})(?<blue>[0-9a-f]{2})(?<alpha>[0-9a-f]{2})?$",
					Pattern.CASE_INSENSITIVE);

	/**
	 * Radix for hexadecimal values
	 */
	private static final int HEX_RADIX = 16;

	/**
	 * Maximum value of an unsigned byte
	 */
	private static final double MAX_UNSIGNED_BYTE_VALUE = 255;

	/**
	 * Pure black
	 */
	public static final Color BLACK = new Color(0, 0, 0, 1);

	/**
	 * Pure blue
	 */
	public static final Color BLUE = new Color(0, 0, 1, 1);

	/**
	 * Pure green
	 */
	public static final Color GREEN = new Color(0, 1, 0, 1);

	/**
	 * Pure red
	 */
	public static final Color RED = new Color(1, 0, 0, 1);

	/**
	 * Pure white
	 */
	public static final Color WHITE = new Color(1, 1, 1, 1);

	/**
	 * Creates a {@link Color} from an hexadecimal value, which can be prefixed with
	 * a hash sign. The alpha value may be omitted.
	 *
	 * @param hex the hexadecimal value
	 * @return the color
	 */
	@JsonCreator
	public static Color fromHex(final String hex) {
		final Matcher matcher = Patterns.matches(HEX_PATTERN, hex)
				.orElseThrow(() -> new IllegalArgumentException(String
						.format("Expecting an hexadecimal color value. Failed parsing \"%s\" as color value.", hex)));
		return new Color(Integer.parseInt(matcher.group("red"), HEX_RADIX) / MAX_UNSIGNED_BYTE_VALUE,
				Integer.parseInt(matcher.group("green"), HEX_RADIX) / MAX_UNSIGNED_BYTE_VALUE,
				Integer.parseInt(matcher.group("blue"), HEX_RADIX) / MAX_UNSIGNED_BYTE_VALUE,
				Integer.parseInt(Nullables.orElse(matcher.group("alpha"), "ff"), HEX_RADIX) / MAX_UNSIGNED_BYTE_VALUE);
	}

	/**
	 * Percentage of the red channel
	 *
	 * @return the percentage of the red channel
	 */
	double red;

	/**
	 * Percentage of the green channel
	 *
	 * @return the green of the red channel
	 */
	double green;

	/**
	 * Percentage of the blue channel
	 *
	 * @return the blue of the red channel
	 */
	double blue;

	/**
	 * Percentage of the alpha channel
	 *
	 * @return the alpha of the red channel
	 */
	double alpha;

	/**
	 * Formats the color in hexadecimal form, prefixed with a hash. The alpha value
	 * may be omitted.
	 *
	 * @return the formatted color
	 */
	@JsonValue
	public String toHex() {
		final String cssColor = String.format("#%02x%02x%02x",
				(int) (MAX_UNSIGNED_BYTE_VALUE * getRed()),
				(int) (MAX_UNSIGNED_BYTE_VALUE * getGreen()),
				(int) (MAX_UNSIGNED_BYTE_VALUE * getBlue()));

		final int alpha = (int) (MAX_UNSIGNED_BYTE_VALUE * getAlpha());
		return cssColor + (alpha < MAX_UNSIGNED_BYTE_VALUE ? String.format("%02x", alpha) : "");
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return toHex();
	}
}
