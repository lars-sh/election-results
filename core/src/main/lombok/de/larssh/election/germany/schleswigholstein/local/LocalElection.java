package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableNavigableMap;
import static java.util.stream.Collectors.toCollection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.Person;
import de.larssh.utils.Nullables;
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
		numberOfDirectSeats.put(0, 3);
		numberOfDirectSeats.put(200, 4);
		numberOfDirectSeats.put(750, 5);
		numberOfDirectSeats.put(1250, 6);
		numberOfDirectSeats.put(2500, 8);
		numberOfDirectSeats.put(5000, 9);
		numberOfDirectSeats.put(10_000, 11);
		numberOfDirectSeats.put(15_000, 13);
		numberOfDirectSeats.put(25_000, 15);
		numberOfDirectSeats.put(35_000, 17);
		numberOfDirectSeats.put(45_000, 19);
		NUMBER_OF_DIRECT_SEATS = unmodifiableNavigableMap(numberOfDirectSeats);
	}

	LocalDistrictSuper district;

	@EqualsAndHashCode.Include
	LocalDate date;

	@EqualsAndHashCode.Include
	String name;

	@NonFinal
	@Getter(AccessLevel.NONE)
	Map<District<?>, OptionalInt> population = new HashMap<>();

	@NonFinal
	@Getter(AccessLevel.NONE)
	Map<District<?>, OptionalInt> numberOfEligibleVoters = new HashMap<>();

	List<LocalNomination> nominations = new ArrayList<>();

	List<LocalBallot> ballots = new ArrayList<>();

	int sainteLagueScale;

	@Override
	public OptionalInt getPopulation(final District<?> district) {
		return Nullables.orElseGet(population.get(district), OptionalInt::empty);
	}

	@Override
	public void setPopulation(final District<?> district, final OptionalInt population) {
		this.population.put(district, population);
	}

	public int getNumberOfSeats() {
		return 2 * getNumberOfListSeats() + 1;
	}

	public int getNumberOfDirectSeats() {
		return getNumberOfListSeats() + 1;
	}

	public int getNumberOfListSeats() {
		final int population = getPopulation();

		if (getDistrict().getType() == LocalDistrictType.KREIS) {
			return population > 200_000 ? 24 : 22;
		}
		if (getDistrict().getType() == LocalDistrictType.KREISFREIE_STADT) {
			return population > 150_000 ? 24 : 21;
		}
		return NUMBER_OF_DIRECT_SEATS.floorEntry(population - 1).getValue();
	}

	public int getNumberOfVotes() {
		return getNumberOfDirectSeats();
	}

	@Override
	public OptionalInt getNumberOfEligibleVoters(final District<?> district) {
		return Nullables.orElseGet(numberOfEligibleVoters.get(district), OptionalInt::empty);
	}

	@Override
	public void setNumberOfEligibleVoters(final District<?> district, final OptionalInt numberOfEligibleVoters) {
		this.numberOfEligibleVoters.put(district, numberOfEligibleVoters);
	}

	public LocalNomination createNomination(final LocalDistrict district,
			final LocalNominationType type,
			final Person person,
			final Optional<Party> party) {
		final LocalNomination nomination = new LocalNomination(this, district, type, person, party);
		nominations.add(nomination);
		return nomination;
	}

	@Override
	public List<LocalNomination> getNominations() {
		return unmodifiableList(nominations);
	}

	public Set<Party> getParties() {
		return getNominations().stream()
				.map(Nomination::getParty)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toCollection(TreeSet::new));
	}

	public LocalBallot createBallot(final LocalPollingStation pollingStation,
			final boolean valid,
			final Set<LocalNomination> nominations,
			final boolean postalVoter) {
		final LocalBallot ballot = new LocalBallot(this, pollingStation, valid, nominations, postalVoter);
		ballots.add(ballot);
		return ballot;
	}

	@Override
	public List<LocalBallot> getBallots() {
		return unmodifiableList(ballots);
	}

	@Override
	public LocalElectionResult getResult(final OptionalInt numberOfBallots) {
		return new LocalElectionResult(this, numberOfBallots, getBallots());
	}
}
