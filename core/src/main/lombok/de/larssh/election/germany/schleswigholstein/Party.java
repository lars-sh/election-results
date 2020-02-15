package de.larssh.election.germany.schleswigholstein;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.paint.Color;
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
			= Comparator.comparing(Party::getType).thenComparing(Party::getShortName).thenComparing(Party::getName);

	@EqualsAndHashCode.Include
	String shortName;

	@EqualsAndHashCode.Include
	String name;

	PartyType type;

	Color backgroundColor;

	Color fontColor;

	@Override
	public int compareTo(@Nullable final Party party) {
		return COMPARATOR.compare(this, party);
	}

	@JsonIgnore
	public String getKey() {
		return String.format("%s (%s)", getName(), Keys.escape(getShortName(), ',', ' '));
	}
}
