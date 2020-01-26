package de.larssh.election.germany.schleswigholstein;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("PMD.ShortClassName")
public class Keys {
	public static String escape(final String value, final char leftCharacter, final char rightCharacter) {
		final String escapeCharacter = "\\";
		return value.replace(escapeCharacter, escapeCharacter + escapeCharacter)
				.replace(Character.toString(leftCharacter) + Character.toString(rightCharacter),
						Character.toString(leftCharacter) + escapeCharacter + Character.toString(rightCharacter));
	}
}
