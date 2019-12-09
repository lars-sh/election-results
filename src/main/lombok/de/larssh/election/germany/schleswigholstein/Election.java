package de.larssh.election.germany.schleswigholstein;

import java.awt.Color;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface Election extends Comparable<Election> {
	Comparator<Election> COMPARATOR = Comparator.<Election, District<?>>comparing(Election::getDistrict)
			.thenComparing(Election::getName)
			.thenComparing(Election::getDate);

	District<?> getDistrict();

	LocalDate getDate();

	String getName();

	default OptionalInt getPopulation() {
		return getPopulation(getDistrict());
	}

	OptionalInt getPopulation(District<?> district);

	default void setPopulation(final District<?> district, final int population) {
		setPopulation(district, OptionalInt.of(population));
	}

	void setPopulation(District<?> district, OptionalInt population);

	default OptionalInt getNumberOfEligibleVoters() {
		return getNumberOfEligibleVoters(getDistrict());
	}

	OptionalInt getNumberOfEligibleVoters(District<?> district);

	default void setNumberOfEligibleVoters(final District<?> district, final int numberOfEligibleVoters) {
		setNumberOfEligibleVoters(district, OptionalInt.of(numberOfEligibleVoters));
	}

	void setNumberOfEligibleVoters(District<?> district, OptionalInt numberOfEligibleVoters);

	List<? extends Nomination> getNominations();

	Color getColorOfBallots();

	OptionalInt getNumberOfAllBallots();

	List<? extends Ballot> getBallots();

	ElectionResult<?> getResult();

	@Override
	default int compareTo(@Nullable final Election election) {
		return COMPARATOR.compare(this, election);
	}
}
