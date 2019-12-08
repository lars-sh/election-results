package de.larssh.election.germany.schleswigholstein;

import java.awt.Color;
import java.util.Comparator;
import java.util.Optional;

import de.larssh.utils.Optionals;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Party implements Comparable<Party> {
	private static final Comparator<Party> COMPARATOR
			= Comparator.comparing(Party::getShortName, Optionals.createComparator()).thenComparing(Party::getName);

	@EqualsAndHashCode.Include
	String name;

	PartyType type;

	Optional<String> shortName;

	Color backgroundColor;

	Color fontColor;

	@Override
	public int compareTo(final Party party) {
		return COMPARATOR.compare(this, party);
	}
}
