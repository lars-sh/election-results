package de.larssh.election.germany.schleswigholstein;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.NonFinal;

/**
 * Politische Partei oder Wählergruppe
 */
@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Party implements Comparable<Party> {
	/**
	 * Comparator by type, short name and full name
	 */
	private static final Comparator<Party> COMPARATOR = Comparator.comparing(Party::getType) //
			.thenComparing(Party::getShortName)
			.thenComparing(Party::getName);

	/**
	 * Short Name
	 *
	 * @return the short name
	 */
	@EqualsAndHashCode.Include
	String shortName;

	/**
	 * Full Name
	 *
	 * @return the full name
	 */
	@EqualsAndHashCode.Include
	String name;

	/**
	 * Type of the party
	 *
	 * @return the type of the party
	 */
	@EqualsAndHashCode.Include
	PartyType type;

	/**
	 * Background color to use in diagrams
	 *
	 * @param backgroundColor the background color
	 * @return the background color
	 */
	@Setter
	@NonFinal
	Color backgroundColor = Color.BLACK;

	/**
	 * Font color to use in diagrams
	 *
	 * @param fontColor the font color
	 * @return the font color
	 */
	@Setter
	@NonFinal
	Color fontColor = Color.WHITE;

	/** {@inheritDoc} */
	@Override
	public int compareTo(@Nullable final Party party) {
		return COMPARATOR.compare(this, party);
	}

	/**
	 * Creates a unique key based on the full and short name.
	 *
	 * @return unique key based on the full and short name
	 */
	@JsonIgnore
	public String getKey() {
		return Keys.escape(getName(), " (", getShortName(), ")");
	}
}
