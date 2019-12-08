package de.larssh.election.germany.schleswigholstein;

import static java.util.function.Predicate.isEqual;

import java.awt.Color;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Predicate;

public interface Election extends Comparable<Election> {
	Comparator<Election> COMPARATOR = Comparator.<Election, District<?>>comparing(Election::getDistrict)
			.thenComparing(Election::getName)
			.thenComparing(Election::getDate);

	District<?> getDistrict();

	LocalDate getDate();

	String getName();

	default int getPopulation() {
		return getPopulation(getDistrict()).orElseThrow(RuntimeException::new); // TODO: Exception
	}

	OptionalInt getPopulation(District<?> district);

	default void setPopulation(final District<?> district, final int population) {
		setPopulation(district, OptionalInt.of(population));
	}

	void setPopulation(District<?> district, OptionalInt population);

	DistrictType getElectionType();

	default int getNumberOfEligibleVoters() {
		return getNumberOfEligibleVoters(getDistrict()).orElseThrow(RuntimeException::new); // TODO: Exception
	}

	OptionalInt getNumberOfEligibleVoters(District<?> district);

	default void setNumberOfEligibleVoters(final District<?> district, final int numberOfEligibleVoters) {
		setNumberOfEligibleVoters(district, OptionalInt.of(numberOfEligibleVoters));
	}

	void setNumberOfEligibleVoters(District<?> district, OptionalInt numberOfEligibleVoters);

	Set<? extends Nomination> getNominations();

	Color getColorOfBallots();

	Set<? extends Ballot> getBallots();

	default ElectionResult getResult() {
		return getResult(isEqual(true));
	}

	ElectionResult getResult(Predicate<? extends Ballot> filter);

	@Override
	default int compareTo(final Election election) {
		return COMPARATOR.compare(this, election);
	}
}
