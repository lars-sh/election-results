package de.larssh.election.germany.schleswigholstein;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Keys {
	public static String escape(final String value, final char leftCharacter, final char rightCharacter) {
		return escape(value, leftCharacter, rightCharacter, '\\');
	}

	public static String escape(final String value,
			final char leftCharacter,
			final char rightCharacter,
			final char escapeCharacter) {
		return value.replace("" + escapeCharacter, "" + escapeCharacter + escapeCharacter)
				.replace("" + leftCharacter + rightCharacter, "" + leftCharacter + escapeCharacter + rightCharacter);
	}
}
