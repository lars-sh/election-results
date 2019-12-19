package de.larssh.election.germany.schleswigholstein;

import java.awt.Color;
import java.util.Comparator;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, onParam_ = { @Nullable })
public class Party implements Comparable<Party> {
	private static final Comparator<Party> COMPARATOR
			= Comparator.comparing(Party::getType).thenComparing(Party::getShortName).thenComparing(Party::getName);

	String name;

	PartyType type;

	@EqualsAndHashCode.Include
	String shortName;

	Color backgroundColor;

	Color fontColor;

	@Override
	public int compareTo(@Nullable final Party party) {
		return COMPARATOR.compare(this, party);
	}
}
