package de.larssh.election.germany.schleswigholstein;

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
	 * Maximum value of an unsigned byte
	 */
	private static final int MAX_UNSIGNED_BYTE_VALUE = 255;

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
	 * Creates a {@link Color} following the RGB color model in unsigned byte
	 * (0-255) values.
	 *
	 * @param red   the unsigned byte value for the red channel
	 * @param green the unsigned byte value for the green channel
	 * @param blue  the unsigned byte value for the blue channel
	 * @return the color
	 */
	public static Color rgb(final int red, final int green, final int blue) {
		return new Color((double) red / MAX_UNSIGNED_BYTE_VALUE,
				(double) green / MAX_UNSIGNED_BYTE_VALUE,
				(double) blue / MAX_UNSIGNED_BYTE_VALUE,
				1);
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

	public String toCssColor() {
		return String.format("rgba(%.0f%%, %.0f%%, %.0f%%, %.0f%%)",
				100 * getRed(),
				100 * getGreen(),
				100 * getBlue(),
				100 * getAlpha());
	}
}
