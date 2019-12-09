package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Collections.unmodifiableNavigableMap;

import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.OptionalInt;
import java.util.TreeMap;

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
	private static final NavigableMap<Integer, Integer> NUMBER_OF_DIRECT_SEATS;

	static {
		final NavigableMap<Integer, Integer> numberOfDirectSeats = new TreeMap<>();
		numberOfDirectSeats.put(0, 4);
		numberOfDirectSeats.put(200, 5);
		numberOfDirectSeats.put(750, 6);
		numberOfDirectSeats.put(1250, 7);
		numberOfDirectSeats.put(2500, 9);
		numberOfDirectSeats.put(5000, 10);
		numberOfDirectSeats.put(10_000, 12);
		numberOfDirectSeats.put(15_000, 14);
		numberOfDirectSeats.put(25_000, 16);
		numberOfDirectSeats.put(35_000, 18);
		numberOfDirectSeats.put(45_000, 20);
		NUMBER_OF_DIRECT_SEATS = unmodifiableNavigableMap(numberOfDirectSeats);
	}

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

	int sainteLagueScale;

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

	public int getNumberOfSeats() {
		return 2 * getNumberOfDirectSeats() - 1;
	}

	public int getNumberOfDirectSeats() {
		final int population = getPopulation();

		if (getDistrict().getType() == LocalDistrictType.KREIS) {
			return population > 200_000 ? 25 : 23;
		}
		if (getDistrict().getType() == LocalDistrictType.KREISFREIE_STADT) {
			return population > 150_000 ? 25 : 22;
		}
		return NUMBER_OF_DIRECT_SEATS.floorEntry(population - 1).getValue();
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
	// @Override
	// default int getNumOfStimmen() {
	// return getNumOfUnmittelbareVertreterPerWahlkreis();
	// }
	//
	// int getNumOfUnmittelbareVertreter();
	//
	// int getNumOfUnmittelbareVertreterPerWahlkreis(); // wtf
	//
	// int getNumOfVertreter(); // max. anzahl sitze

	// TODO: wahlbeteiligung
}
