package de.larssh.election.germany.schleswigholstein;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Politische Partei oder Wählergruppe
 */
@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Party implements Comparable<Party> {
	private static final Comparator<Party> COMPARATOR
			= Comparator.comparing(Party::getType).thenComparing(Party::getShortName).thenComparing(Party::getName);

	private static String createKey(final String name, final String shortName) {
		return Keys.escape(name, " (", shortName, ")");
	}

	@EqualsAndHashCode.Include
	String shortName;

	@EqualsAndHashCode.Include
	String name;

	PartyType type;

	Color backgroundColor;

	Color fontColor;

	Optional<Path> logo;

	@Override
	public int compareTo(@Nullable final Party party) {
		return COMPARATOR.compare(this, party);
	}

	@JsonIgnore
	public String getKey() {
		return createKey(getName(), getShortName());
	}
}
