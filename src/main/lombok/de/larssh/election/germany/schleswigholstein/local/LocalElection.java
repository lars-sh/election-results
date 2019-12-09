package de.larssh.election.germany.schleswigholstein.local;

import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.Election;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.NonFinal;

@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, onParam_ = { @Nullable })
public class LocalElection implements Election {
	LocalDistrictSuper district;

	@EqualsAndHashCode.Include
	LocalDate date;

	@EqualsAndHashCode.Include
	String name;

	@NonFinal
	@Getter(AccessLevel.NONE)
	Map<District<?>, OptionalInt> population = new HashMap<>();// TODO: getter

	@NonFinal
	@Getter(AccessLevel.NONE)
	Map<District<?>, OptionalInt> numberOfEligibleVoters = new HashMap<>();// TODO: getter

	List<LocalNomination> nominations = new ArrayList<>(); // TODO: getter

	OptionalInt numberOfAllBallots;

	List<LocalBallot> ballots = new ArrayList<>(); // TODO: getter

	@Override
	public Color getColorOfBallots() {
		return getDistrict().getType() == LocalDistrictType.KREIS ? Color.RED : Color.WHITE;
	}

	@Override
	public OptionalInt getPopulation(final District<?> district) {
		throw new UnsupportedOperationException(); // TODO: implement
	}

	@Override
	public void setPopulation(final District<?> district, final OptionalInt population) {
		throw new UnsupportedOperationException(); // TODO: implement
	}

	@Override
	public OptionalInt getNumberOfEligibleVoters(final District<?> district) {
		throw new UnsupportedOperationException(); // TODO: implement
	}

	@Override
	public void setNumberOfEligibleVoters(final District<?> district, final OptionalInt numberOfEligibleVoters) {
		throw new UnsupportedOperationException(); // TODO: implement
	}

	public LocalNomination createNomination() {
		throw new UnsupportedOperationException(); // TODO: implement
	}

	@Override
	public LocalElectionResult getResult() {
		return new LocalElectionResult(this, getBallots());
	}

	// TODO
	// default int getNumOfListenvertreter() {
	// return getNumOfVertreter() - getNumOfUnmittelbareVertreter();
	// }
	//
	// @Override
	// default int getNumOfStimmen() {
	// return getNumOfUnmittelbareVertreterPerWahlkreis();
	// }
	//
	// int getNumOfUnmittelbareVertreter();
	//
	// int getNumOfUnmittelbareVertreterPerWahlkreis();
	//
	// int getNumOfVertreter();
}
