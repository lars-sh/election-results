package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toLinkedHashMap;
import static de.larssh.utils.Collectors.toLinkedHashSet;
import static de.larssh.utils.Collectors.toMap;
import static de.larssh.utils.Finals.constant;
import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.ElectionResult;
import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.PartyResult;
import de.larssh.election.germany.schleswigholstein.local.LocalElection.ParsableLocalNomination;
import de.larssh.utils.OptionalInts;
import de.larssh.utils.annotations.PackagePrivate;
import de.larssh.utils.collection.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@SuppressWarnings({ "PMD.DataClass", "PMD.ExcessiveImports" })
public final class LocalElectionResult implements ElectionResult<LocalBallot> {
	@PackagePrivate
	static final ThreadLocal<LocalElection> ELECTION_FOR_JSON_CREATOR = ThreadLocal.withInitial(() -> {
		throw new ElectionException(
				"Cannot initialize electionForJsonCreator. Use LocalElection.fromJson(...) instead.");
	});

	public static final int SAINTE_LAGUE_SCALE_DEFAULT = constant(2);

	public static ObjectWriter createJacksonObjectWriter() {
		return LocalElection.createJacksonObjectWriter();
	}

	public static LocalElectionResult fromJson(final Reader reader, final LocalElection election) throws IOException {
		ELECTION_FOR_JSON_CREATOR.set(election);
		final LocalElectionResult electionResult
				= LocalElection.OBJECT_MAPPER.readValue(reader, LocalElectionResult.class);
		ELECTION_FOR_JSON_CREATOR.remove();

		return electionResult;
	}

	@JsonIgnore
	@ToString.Exclude
	LocalElection election;

	OptionalInt numberOfAllBallots;

	List<LocalBallot> ballots;

	@JsonIgnore
	Map<LocalNomination, LocalNominationResult> nominationResults;

	@JsonIgnore
	Map<Party, LocalPartyResult> partyResults;

	@JsonIgnore
	Supplier<Integer> numberOfInvalidBallots
			= lazy(() -> (int) getBallots().stream().filter(ballot -> !ballot.isValid()).count());

	@JsonCreator(mode = Mode.DELEGATING)
	private LocalElectionResult(final ParsableLocalElectionResult parsable) {
		this(ELECTION_FOR_JSON_CREATOR.get(), parsable.getNumberOfAllBallots(), parsable.createLocalBallots());
	}

	public LocalElectionResult(final LocalElection election,
			final OptionalInt numberOfAllBallots,
			final Collection<LocalBallot> ballots) {
		this(election, numberOfAllBallots, ballots, ballot -> true);
	}

	private LocalElectionResult(final LocalElection election,
			final OptionalInt numberOfAllBallots,
			final Collection<LocalBallot> ballots,
			final Predicate<LocalBallot> filter) {
		this.election = election;
		this.ballots = unmodifiableList(ballots.stream().filter(filter).collect(toList()));
		this.numberOfAllBallots = numberOfAllBallots;

		for (final LocalBallot ballot : ballots) {
			if (!ballot.getElection().equals(election)) {
				throw new ElectionException("Election \"%s\" of ballot does not match given election \"%s\".",
						ballot.getElection().getName(),
						election.getName());
			}
		}

		nominationResults = unmodifiableMap(createNominationResults());
		partyResults = unmodifiableMap(createPartyResults());
	}

	@Override
	public LocalElectionResult filter(final Predicate<LocalBallot> filter) {
		return new LocalElectionResult(getElection(), OptionalInt.empty(), getBallots(), filter);
	}

	public Optional<BigDecimal> getCountingProgress(final int scale) {
		return OptionalInts.mapToObj(getNumberOfAllBallots(),
				numberOfAllBallots -> BigDecimal.valueOf(getBallots().size())
						.multiply(BigDecimal.TEN)
						.multiply(BigDecimal.TEN)
						.divide(BigDecimal.valueOf(numberOfAllBallots), scale, RoundingMode.HALF_UP));
	}

	public int getNumberOfInvalidBallots() {
		return numberOfInvalidBallots.get();
	}

	private Map<LocalNomination, LocalNominationResult> createNominationResults() {
		final Map<LocalNomination, Integer> votes = getVotes();
		final Map<Party, Integer> votesOfParties = getVotesOfParty();

		// Result Type: Direct
		final Map<LocalNomination, LocalNominationResultType> resultTypes
				= new LinkedHashMap<>(getElection().getNominations().size());
		for (final LocalNomination nomination : getDirectNominations(votes)) {
			resultTypes.put(nomination, LocalNominationResultType.DIRECT);
		}

		// Result Type: Direct Draw
		for (final LocalNomination nomination : getDrawNominations(votes, resultTypes)) {
			resultTypes.put(nomination, LocalNominationResultType.DIRECT_DRAW);
		}

		// Result Type: List
		final Map<LocalNomination, BigDecimal> sainteLague = getSainteLague(votes, votesOfParties);
		for (final LocalNomination nomination : getListNominations(votes, sainteLague)) {
			resultTypes.putIfAbsent(nomination, LocalNominationResultType.LIST);
		}

		// Result Type: Direct Balance Seat
		// TODO: Direct Balance Seat

		// Result Type: List Overhang Seat
		// TODO: List Overhang Seat

		// Result Type: List Draw
		for (final LocalNomination nomination : getDrawNominations(sainteLague, resultTypes)) {
			resultTypes.put(nomination, LocalNominationResultType.LIST_DRAW);
		}

		// Result Type: Not Elected
		return getElection().getNominations()
				.stream()
				.map(nomination -> new LocalNominationResult(this,
						nomination,
						resultTypes.getOrDefault(nomination, LocalNominationResultType.NOT_ELECTED),
						sainteLague.getOrDefault(nomination, BigDecimal.ZERO)))
				.sorted()
				.collect(toLinkedHashMap(LocalNominationResult::getNomination, Function.identity()));
	}

	private Map<LocalNomination, Integer> getVotes() {
		// Calculate
		final Map<LocalNomination, Integer> votes = getBallots().stream()
				.filter(Ballot::isValid)
				.map(LocalBallot::getNominations)
				.flatMap(Set::stream)
				.collect(toMap(identity(), nomination -> 1, (oldValue, thisValue) -> oldValue + thisValue));

		// Sort
		return Maps.sort(votes,
				Comparator.<Entry<LocalNomination, Integer>, Integer>comparing(Entry::getValue)
						.thenComparing(Entry::getKey));
	}

	private Map<Party, Integer> getVotesOfParty() {
		// Calculate
		final Map<Party, Integer> votes = getBallots().stream()
				.filter(Ballot::isValid)
				.map(Ballot::getNominations)
				.flatMap(Set::stream)
				.map(Nomination::getParty)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toMap(identity(), party -> 1, (oldValue, thisValue) -> oldValue + thisValue));

		// Sort
		return Maps.sort(votes,
				Comparator.<Entry<Party, Integer>, Integer>comparing(Entry::getValue).thenComparing(Entry::getKey));
	}

	private Map<LocalNomination, BigDecimal> getSainteLague(final Map<LocalNomination, Integer> votes,
			final Map<Party, Integer> votesOfParties) {
		// Calculate
		final Map<LocalNomination, BigDecimal> sainteLague = votesOfParties.entrySet()
				.stream()
				.map(entry -> getSainteLagueOfParty(votes, entry.getKey(), entry.getValue()))
				.map(Map::entrySet)
				.flatMap(Set::stream)
				.collect(toLinkedHashMap());

		// Sort
		final List<LocalNomination> nominations = getElection().getNominations();
		return Maps.sort(sainteLague,
				Comparator.<Entry<LocalNomination, BigDecimal>, BigDecimal>comparing(Entry::getValue)
						.reversed()
						.thenComparing(entry -> -votes.get(entry.getKey()))
						.thenComparing(entry -> nominations.indexOf(entry.getKey())));
	}

	private Map<LocalNomination, BigDecimal> getSainteLagueOfParty(final Map<LocalNomination, Integer> votes,
			final Party party,
			final int votesOfParty) {
		final Set<LocalNomination> nominationsOfParty = getSainteLagueNominationsOfParty(votes, party);
		final BigDecimal votesOfPartyAsBigDecimal = BigDecimal.valueOf(votesOfParty);
		final Iterator<LocalNomination> iterator = nominationsOfParty.iterator();

		return IntStream.range(0, nominationsOfParty.size())
				.mapToObj(step -> getSainteLagueValue(votesOfPartyAsBigDecimal, step))
				.collect(toLinkedHashMap(step -> iterator.next(), identity()));
	}

	private Set<LocalNomination> getSainteLagueNominationsOfParty(final Map<LocalNomination, Integer> votes,
			final Party party) {
		final Set<LocalNomination> nominations = new LinkedHashSet<>();

		// Result Type: Direct and Direct Draw
		getDirectNominations(votes).stream()
				.filter(nomination -> nomination.getParty().map(party::equals).orElse(Boolean.FALSE))
				.forEach(nominations::add);

		// others
		nominations.addAll(getElection().getNominationsOfParty(party));

		return nominations;
	}

	@SuppressWarnings("checkstyle:MagicNumber")
	private BigDecimal getSainteLagueValue(final BigDecimal votesOfParty, final int step) {
		return votesOfParty.divide(BigDecimal.valueOf(step * 10L + 5, 1),
				getElection().getSainteLagueScale(),
				RoundingMode.HALF_UP);
	}

	private Set<LocalNomination> getDirectNominations(final Map<LocalNomination, Integer> votes) {
		return votes.keySet().stream().limit(getElection().getNumberOfDirectSeats()).collect(toLinkedHashSet());
	}

	private Set<LocalNomination> getDrawNominations(final Map<LocalNomination, ? extends Number> votes,
			final Map<LocalNomination, LocalNominationResultType> resultTypes) {
		// Find last nomination
		final Optional<? extends Number> lastNomination = resultTypes.isEmpty()
				? Optional.empty()
				: votes.values().stream().skip(resultTypes.size() - 1L).findAny();
		if (!lastNomination.isPresent()) {
			return emptySet();
		}

		// Collect probably draw nominations
		final Set<LocalNomination> probablyDirectNominations = votes.entrySet()
				.stream()
				.filter(entry -> entry.getValue().equals(lastNomination.get()))
				.map(Entry::getKey)
				.collect(toCollection(TreeSet::new));
		// TODO: Result of the draws

		return probablyDirectNominations.size() > 1 ? probablyDirectNominations : emptySet();
	}

	private Set<LocalNomination> getListNominations(final Map<LocalNomination, Integer> votes,
			final Map<LocalNomination, BigDecimal> sainteLague) {
		final Set<LocalNomination> directNominations = getDirectNominations(votes);
		final int numberOfSeats = getElection().getNumberOfSeats();

		final Set<LocalNomination> nominations = new LinkedHashSet<>(numberOfSeats);
		for (final LocalNomination nomination : sainteLague.keySet()) {
			if (nominations.size() >= numberOfSeats && nominations.containsAll(directNominations)) {
				return nominations;
			}
			nominations.add(nomination);
		}
		return nominations;
	}

	private Map<Party, LocalPartyResult> createPartyResults() {
		return getElection().getParties()
				.stream()
				.map(party -> new LocalPartyResult(this, party))
				.sorted()
				.collect(toLinkedHashMap(PartyResult::getParty, Function.identity()));
	}

	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalElectionResult {
		OptionalInt numberOfAllBallots;

		Collection<ParsableLocalBallot> ballots;

		public Collection<LocalBallot> createLocalBallots() {
			final LocalElection election = ELECTION_FOR_JSON_CREATOR.get();
			return getBallots().stream()
					.map(ballot -> ballot.isValid()
							? LocalBallot.createValidBallot(election,
									ballot.findPollingStation(),
									ballot.isPostalVoter(),
									ballot.findNominations())
							: LocalBallot
									.createInvalidBallot(election, ballot.findPollingStation(), ballot.isPostalVoter()))
					.collect(toList());
		}
	}

	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalBallot {
		String pollingStation;

		boolean postalVoter;

		boolean valid;

		Set<ParsableLocalNomination> nominations;

		public LocalPollingStation findPollingStation() {
			return ELECTION_FOR_JSON_CREATOR.get()
					.getDistrict()
					.getChildren()
					.stream()
					.map(LocalDistrict::getChildren)
					.flatMap(Collection::stream)
					.filter(pollingStation -> getPollingStation().equals(pollingStation.getKey()))
					.findAny()
					.orElseThrow(() -> new ElectionException(
							"Could not find polling station with key \"%s\" for election \"%s\".",
							getPollingStation(),
							ELECTION_FOR_JSON_CREATOR.get().getName()));
		}

		public Set<LocalNomination> findNominations() {
			final Map<String, LocalNomination> nominations = ELECTION_FOR_JSON_CREATOR.get()
					.getNominations()
					.stream()
					.collect(toMap(LocalNomination::getKey, identity()));

			return getNominations().stream()
					.map(nomination -> LocalNomination.createKey(nomination.getPerson().getKey(),
							nomination.getParty()))
					.map(key -> {
						final LocalNomination nomination = nominations.get(key);
						if (nomination == null) {
							throw new ElectionException(
									"Could not find nomination with key \"%s\" for election \"%s\".",
									key,
									ELECTION_FOR_JSON_CREATOR.get().getName());
						}
						return nomination;
					})
					.collect(toSet());
		}
	}
}
