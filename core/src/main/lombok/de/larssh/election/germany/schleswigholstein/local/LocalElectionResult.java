package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toLinkedHashMap;
import static de.larssh.utils.Collectors.toLinkedHashSet;
import static de.larssh.utils.Collectors.toMap;
import static de.larssh.utils.Finals.constant;
import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
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
import de.larssh.utils.Nullables;
import de.larssh.utils.OptionalInts;
import de.larssh.utils.annotations.PackagePrivate;
import de.larssh.utils.collection.Maps;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@SuppressWarnings({ "PMD.DataClass", "PMD.ExcessiveImports" })
public final class LocalElectionResult implements ElectionResult<LocalBallot, LocalNomination> {
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

	@ToString.Exclude
	Set<LocalNomination> directDrawResults;

	@ToString.Exclude
	Set<LocalNomination> listDrawResults;

	@JsonIgnore
	@ToString.Exclude
	Map<LocalNomination, LocalNominationResult> nominationResults;

	@JsonIgnore
	@ToString.Exclude
	Map<Party, LocalPartyResult> partyResults;

	@JsonIgnore
	Supplier<Integer> numberOfInvalidBallots
			= lazy(() -> (int) getBallots().stream().filter(ballot -> !ballot.isValid()).count());

	@JsonCreator(mode = Mode.DELEGATING)
	private LocalElectionResult(final ParsableLocalElectionResult parsable) {
		this(ELECTION_FOR_JSON_CREATOR.get(),
				parsable.getNumberOfAllBallots(),
				parsable.createLocalBallots(),
				parsable.createDirectDrawResults(),
				parsable.createListDrawResults());
	}

	public LocalElectionResult(final LocalElection election,
			final OptionalInt numberOfAllBallots,
			final List<LocalBallot> ballots,
			final Set<LocalNomination> directDrawResults,
			final Set<LocalNomination> listDrawResults) {
		this(election, numberOfAllBallots, ballots, directDrawResults, listDrawResults, ballot -> true);
	}

	@SuppressFBWarnings(value = "OCP_OVERLY_CONCRETE_PARAMETER",
			justification = "Parameter \"ballots\" should be in order and therefore must not be of type Collection.")
	private LocalElectionResult(final LocalElection election,
			final OptionalInt numberOfAllBallots,
			final List<LocalBallot> ballots,
			final Set<LocalNomination> directDrawResults,
			final Set<LocalNomination> listDrawResults,
			final Predicate<? super LocalBallot> filter) {
		this.election = election;
		this.ballots = unmodifiableList(ballots.stream().filter(filter).collect(toList()));
		this.numberOfAllBallots = numberOfAllBallots;
		this.directDrawResults = unmodifiableSet(new HashSet<>(directDrawResults));
		this.listDrawResults = unmodifiableSet(new HashSet<>(listDrawResults));

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
	public LocalElectionResult filter(final Predicate<? super LocalBallot> filter) {
		return new LocalElectionResult(getElection(),
				OptionalInt.empty(),
				getBallots(),
				getDirectDrawResults(),
				getListDrawResults(),
				filter);
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

	@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.NPathComplexity" })
	private Map<LocalNomination, LocalNominationResult> createNominationResults() {
		// TODO: Calculate results per LocalDistrict
		final Map<LocalNomination, Integer> votes = getVotes();
		final Map<Party, Integer> votesOfParties = getVotesOfParty();

		// Result Type: Direct
		final Map<LocalNomination, LocalNominationResultType> resultTypes
				= new LinkedHashMap<>(getElection().getNominations().size());
		for (final LocalNomination nomination : getDirectNominations(votes)) {
			resultTypes.put(nomination, LocalNominationResultType.DIRECT);
		}

		// Result Type: Direct Draw
		for (final Entry<LocalNomination, LocalNominationResultType> entry : getDrawNominations(resultTypes,
				votes,
				getDirectDrawResults(),
				LocalNominationResultType.DIRECT,
				LocalNominationResultType.DIRECT_DRAW).entrySet()) {
			resultTypes.put(entry.getKey(), entry.getValue());
		}

		// Result Type: List
		final Map<LocalNomination, BigDecimal> sainteLague = getSainteLague(votes, votesOfParties);
		for (final LocalNomination nomination : getListNominations(votes, sainteLague)) {
			resultTypes.putIfAbsent(nomination, LocalNominationResultType.LIST);
		}

		// Result Type: List Draw
		for (final Entry<LocalNomination, LocalNominationResultType> entry : getDrawNominations(resultTypes,
				sainteLague,
				getListDrawResults(),
				LocalNominationResultType.LIST,
				LocalNominationResultType.LIST_DRAW).entrySet()) {
			resultTypes.put(entry.getKey(), entry.getValue());
		}

		// Result Type: Direct Balance Seat
		final Set<LocalNomination> balanceAndOverhangSeats = getBalanceAndOverhangSeats(sainteLague, resultTypes);
		for (final LocalNomination nomination : balanceAndOverhangSeats) {
			if (resultTypes.get(nomination) == LocalNominationResultType.DIRECT) {
				resultTypes.put(nomination, LocalNominationResultType.DIRECT_BALANCE_SEAT);
			}
		}

		// Result Type: List Overhang Seat
		for (final LocalNomination nomination : balanceAndOverhangSeats) {
			if (resultTypes.get(nomination) == LocalNominationResultType.LIST) {
				resultTypes.put(nomination, LocalNominationResultType.LIST_OVERHANG_SEAT);
			}
		}

		// Result Type: Not Elected
		return getElection().getNominations()
				.stream()
				.map(nomination -> new LocalNominationResult(this,
						nomination,
						resultTypes.getOrDefault(nomination, LocalNominationResultType.NOT_ELECTED),
						sainteLague.getOrDefault(nomination, BigDecimal.ZERO)))
				.sorted()
				.collect(toLinkedHashMap(LocalNominationResult::getNomination, identity()));
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
						.reversed()
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
						.thenComparing(entry -> votes.getOrDefault(entry.getKey(), 0))
						.reversed()
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

	private Map<LocalNomination, LocalNominationResultType> getDrawNominations(
			final Map<LocalNomination, LocalNominationResultType> resultTypes,
			final Map<LocalNomination, ? extends Number> votes,
			final Set<LocalNomination> drawResults,
			final LocalNominationResultType currentResultType,
			final LocalNominationResultType currentResultTypeDraw) {
		if (resultTypes.isEmpty()) {
			return emptyMap();
		}
		final Number votesForLastNomination
				= votes.get(resultTypes.keySet().stream().reduce((first, second) -> second).get());

		final int numberOfPossibleSeats = resultTypes.size();
		resultTypes.entrySet()
				.removeIf(entry -> entry.getValue() == currentResultType
						&& votes.get(entry.getKey()).equals(votesForLastNomination)); // TODO: Do not modify from here!
		final int numberOfDrawSeats = numberOfPossibleSeats - resultTypes.size();

		// Draw Results
		if (drawResults.size() > numberOfDrawSeats) {
			throw new ElectionException("%d %s draw results given while expecting %d %s draw results at max.",
					drawResults.size(),
					currentResultType.toString().toLowerCase(Locale.ROOT),
					numberOfDrawSeats,
					currentResultType.toString().toLowerCase(Locale.ROOT));
		}

		final Map<LocalNomination, LocalNominationResultType> returningResultTypes = new LinkedHashMap<>();
		for (final LocalNomination nomination : drawResults) {
			returningResultTypes.put(nomination, currentResultType);
			if (!Nullables.orElse(votes.get(nomination), 0).equals(votesForLastNomination) // TODO: Test
					|| resultTypes.getOrDefault(nomination, currentResultType) != currentResultType) {
				throw new ElectionException("\"%s\" must not be part of the %s draw results.",
						nomination.getPerson().getKey(),
						currentResultType.toString().toLowerCase(Locale.ROOT));
			}
		}

		// Draws
		if (numberOfDrawSeats > drawResults.size()) {
			final Set<LocalNomination> drawNominations = votes.entrySet()
					.stream()
					.filter(entry -> entry.getValue().equals(votesForLastNomination))
					.map(Entry::getKey)
					.filter(nomination -> resultTypes.getOrDefault(nomination, currentResultType) == currentResultType)
					.collect(toSet());
			final LocalNominationResultType resultType
					= drawNominations.size() > numberOfDrawSeats ? currentResultTypeDraw : currentResultType;
			for (final LocalNomination nomination : drawNominations) {
				returningResultTypes.putIfAbsent(nomination, resultType);
			}
		}

		return returningResultTypes;
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

	private Set<LocalNomination> getBalanceAndOverhangSeats(final Map<LocalNomination, BigDecimal> sainteLague,
			final Map<LocalNomination, LocalNominationResultType> resultTypes) {
		return sainteLague.keySet()
				.stream()
				.limit(resultTypes.size())
				.skip(getElection().getNumberOfSeats())
				.collect(toSet());
	}

	private Map<Party, LocalPartyResult> createPartyResults() {
		return getElection().getParties()
				.stream()
				.map(party -> new LocalPartyResult(this, party))
				.sorted()
				.collect(toLinkedHashMap(PartyResult::getParty, identity()));
	}

	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalElectionResult {
		OptionalInt numberOfAllBallots;

		List<ParsableLocalBallot> ballots;

		Set<ParsableLocalNomination> directDrawResults;

		Set<ParsableLocalNomination> listDrawResults;

		public List<LocalBallot> createLocalBallots() {
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

		public Set<LocalNomination> createDirectDrawResults() {
			return ParsableLocalNomination.createSet(ELECTION_FOR_JSON_CREATOR.get(), getDirectDrawResults());
		}

		public Set<LocalNomination> createListDrawResults() {
			return ParsableLocalNomination.createSet(ELECTION_FOR_JSON_CREATOR.get(), getListDrawResults());
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
			return ParsableLocalNomination.createSet(ELECTION_FOR_JSON_CREATOR.get(), getNominations());
		}
	}
}
