package de.larssh.election.germany.schleswigholstein;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("PMD.ShortClassName")
public class Keys {
	private static final String ESCAPE_CHARACTER = "\\";

	public static String escape(final String value, final char leftCharacter, final char rightCharacter) {
		return value.replace(ESCAPE_CHARACTER, ESCAPE_CHARACTER + ESCAPE_CHARACTER)
				.replace(Character.toString(leftCharacter) + Character.toString(rightCharacter),
						Character.toString(leftCharacter) + ESCAPE_CHARACTER + Character.toString(rightCharacter));
	}
}
