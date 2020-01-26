package de.larssh.election.germany.schleswigholstein;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("PMD.ShortClassName")
public class Keys {
	public static String escape(final String value, final char leftCharacter, final char rightCharacter) {
		return escape(value, leftCharacter, rightCharacter, '\\');
	}

	public static String escape(final String value,
			final char leftCharacter,
			final char rightCharacter,
			final char escapeCharacter) {
		return value
				.replace(Character.toString(escapeCharacter),
						Character.toString(escapeCharacter) + Character.toString(escapeCharacter))
				.replace(Character.toString(leftCharacter) + Character.toString(rightCharacter),
						Character.toString(leftCharacter)
								+ Character.toString(escapeCharacter)
								+ Character.toString(rightCharacter));
	}
}
