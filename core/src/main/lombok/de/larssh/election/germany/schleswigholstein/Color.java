package de.larssh.election.germany.schleswigholstein;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class Color {
	public static final Color BLACK = new Color(0, 0, 0, 1);

	public static final Color BLUE = new Color(0, 0, 1, 1);

	public static final Color GREEN = new Color(0, 1, 0, 1);

	public static final Color RED = new Color(1, 0, 0, 1);

	public static final Color WHITE = new Color(1, 1, 1, 1);

	public static Color rgb(final int red, final int green, final int blue) {
		return new Color((double) red / 255, (double) green / 255, (double) blue / 255, 1);
	}

	double red;

	double green;

	double blue;

	double opacity;
}
