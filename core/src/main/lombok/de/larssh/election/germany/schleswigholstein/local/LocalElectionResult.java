package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toLinkedHashMap;
import static de.larssh.utils.Collectors.toLinkedHashSet;
import static de.larssh.utils.Collectors.toMap;
import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.emptySet;
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

/**
 * Wahlergebnis auf Basis einer ggf. gefilterten Liste an Stimmzetteln
 */
@Getter
@ToString
@SuppressWarnings({ "PMD.DataClass", "PMD.ExcessiveImports", "PMD.GodClass" })
public final class LocalElectionResult implements ElectionResult<LocalBallot, LocalNomination> {
	/**
	 * Thread-local temporary storage of a {@link LocalElection}, used while parsing
	 * JSON data.
	 *
	 * <p>
	 * This thread-local variable is empty and must not be set outside of
	 * {@link #fromJson(Reader, LocalElection)}.
	 */
	@PackagePrivate
	static final ThreadLocal<LocalElection> ELECTION_FOR_JSON_CREATOR = ThreadLocal.withInitial(() -> {
		throw new ElectionException(
				"Cannot initialize electionForJsonCreator. Use LocalElection.fromJson(...) instead.");
	});

	/**
	 * Comparator by value (high to low) and nomination
	 */
	private static final Comparator<Entry<LocalNomination, Integer>> VOTES_OF_NOMINATIONS_COMPARATOR
			= Comparator.<Entry<LocalNomination, Integer>, Integer>comparing(Entry::getValue)
					.reversed()
					.thenComparing(Entry::getKey);

	/**
	 * Comparator by value (low to high) and party
	 */
	private static final Comparator<Entry<Party, Integer>> VOTES_OF_PARTY_COMPARATOR
			= Comparator.<Entry<Party, Integer>, Integer>comparing(Entry::getValue).thenComparing(Entry::getKey);

	/**
	 * Creates a new JSON {@link ObjectWriter} compatible with
	 * {@link LocalElectionResult}.
	 *
	 * @return the created JSON {@link ObjectWriter}
	 */
	public static ObjectWriter createJacksonObjectWriter() {
		return LocalElection.createJacksonObjectWriter();
	}

	/**
	 * Creates a {@link LocalElectionResult} from JSON.
	 *
	 * @param reader   JSON data
	 * @param election Wahl
	 * @return the created {@link LocalElectionResult}
	 * @throws IOException on IO error
	 */
	public static LocalElectionResult fromJson(final Reader reader, final LocalElection election) throws IOException {
		try {
			ELECTION_FOR_JSON_CREATOR.set(election);
			return LocalElection.OBJECT_MAPPER.readValue(reader, LocalElectionResult.class);
		} finally {
			ELECTION_FOR_JSON_CREATOR.remove();
		}
	}

	/**
	 * Wahl
	 *
	 * @return Wahl
	 */
	@JsonIgnore
	@ToString.Exclude
	LocalElection election;

	/**
	 * Optional number of all ballots of the election
	 *
	 * <p>
	 * In case this number is larger than the size of the list of ballots, then some
	 * ballots were not evaluated, yet.
	 *
	 * @return the number of all ballots of the election or empty
	 */
	OptionalInt numberOfAllBallots;

	/**
	 * Stimmzettel
	 *
	 * @return Stimmzettel
	 */
	List<LocalBallot> ballots;

	/**
	 * Ausgeloste Loskandidaten mit Direktmandat
	 *
	 * @return Ausgeloste Loskandidaten mit Direktmandat
	 */
	@ToString.Exclude
	Set<LocalNomination> directDrawResults;

	/**
	 * Ausgeloste Loskandidaten mit Listenmandat
	 *
	 * @return Ausgeloste Loskandidaten mit Listenmandat
	 */
	@ToString.Exclude
	Set<LocalNomination> listDrawResults;

	/**
	 * Wahlergebnis einzelner Bewerberinnen und Bewerber
	 *
	 * @return Wahlergebnis einzelner Bewerberinnen und Bewerber
	 */
	@JsonIgnore
	@ToString.Exclude
	Map<LocalNomination, LocalNominationResult> nominationResults;

	/**
	 * Wahlergebnis einzelner politischer Parteien und Wählergruppen
	 *
	 * @return Wahlergebnis einzelner politischer Parteien und Wählergruppen
	 */
	@JsonIgnore
	@ToString.Exclude
	Map<Party, LocalPartyResult> partyResults;

	/**
	 * Number of invalid ballots
	 */
	@JsonIgnore
	Supplier<Integer> numberOfInvalidBallots
			= lazy(() -> (int) getBallots().stream().filter(ballot -> !ballot.isValid()).count());

	/**
	 * Wahlergebnis
	 *
	 * @param parsable JSON delegate
	 */
	@JsonCreator(mode = Mode.DELEGATING)
	private LocalElectionResult(final ParsableLocalElectionResult parsable) {
		this(ELECTION_FOR_JSON_CREATOR.get(),
				parsable.getNumberOfAllBallots(),
				parsable.getLocalBallots(),
				parsable.getDirectDrawResults(),
				parsable.getListDrawResults());
	}

	/**
	 * Wahlergebnis
	 *
	 * @param election           Wahl
	 * @param numberOfAllBallots optional number of all ballots of the election
	 * @param ballots            Stimmzettel
	 * @param directDrawResults  Ausgeloste Loskandidaten mit Direktmandat
	 * @param listDrawResults    Ausgeloste Loskandidaten mit Listenmandat
	 */
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Election is no longer modifiable when passed here.")
	public LocalElectionResult(final LocalElection election,
			final OptionalInt numberOfAllBallots,
			final List<LocalBallot> ballots,
			final Set<LocalNomination> directDrawResults,
			final Set<LocalNomination> listDrawResults) {
		this.election = election;
		this.ballots = unmodifiableList(ballots);
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

		// Creating the nomination results first, because party results are based on the
		// nomination results
		nominationResults = unmodifiableMap(createNominationResults());
		partyResults = unmodifiableMap(createPartyResults());
	}

	/** {@inheritDoc} */
	@Override
	public LocalElectionResult filter(final Predicate<? super LocalBallot> filter) {
		final List<LocalBallot> filteredBallots = getBallots().stream().filter(filter).collect(toList());
		final OptionalInt numberOfFilteredBallots = OptionalInts
				.filter(getNumberOfAllBallots(), numberOfAllBallots -> numberOfAllBallots == getBallots().size())
				.isPresent() ? OptionalInt.of(filteredBallots.size()) : OptionalInt.empty();

		return new LocalElectionResult(getElection(),
				numberOfFilteredBallots,
				filteredBallots,
				getDirectDrawResults(),
				getListDrawResults());
	}

	/**
	 * Calculates the ballot evaluation progress in percentage.
	 *
	 * @param scale the scale of the {@link BigDecimal} quotient
	 * @return the ballot evaluation progress in percentage or empty if the number
	 *         of all ballots is empty
	 */
	public Optional<BigDecimal> getEvaluationProgress(final int scale) {
		return OptionalInts.mapToObj(getNumberOfAllBallots(),
				numberOfAllBallots -> BigDecimal.valueOf(getBallots().size())
						.multiply(BigDecimal.TEN)
						.multiply(BigDecimal.TEN)
						.divide(BigDecimal.valueOf(numberOfAllBallots), scale, RoundingMode.HALF_UP));
	}

	/**
	 * Ausgeloste Loskandidaten mit Direktmandat eines Wahlkreises
	 *
	 * @param district Wahlkreis
	 * @return Ausgeloste Loskandidaten mit Direktmandat eines Wahlkreises
	 */
	@JsonIgnore
	public Set<LocalNomination> getDirectDrawResults(final LocalDistrict district) {
		return getDirectDrawResults().stream()
				.filter(nomination -> nomination.getDistrict().equals(district))
				.collect(toSet());
	}

	/**
	 * Number of invalid ballots
	 *
	 * @return the number of invalid ballots
	 */
	public int getNumberOfInvalidBallots() {
		return numberOfInvalidBallots.get();
	}

	/**
	 * Calculates the nomination results.
	 *
	 * <p>
	 * This method is used within the method constructor. Except for the balance
	 * seats the implementation is structured based on the order of
	 * {@link LocalNominationResultType}.
	 *
	 * @return Wahlergebnis einzelner Bewerberinnen und Bewerber
	 */
	private Map<LocalNomination, LocalNominationResult> createNominationResults() {
		final Map<LocalNomination, Integer> votes = getVotesOfNominations();
		final Map<Party, Integer> votesOfParties = getVotesOfParties();
		final Map<LocalNomination, LocalNominationResultType> resultTypes
				= new LinkedHashMap<>(getElection().getNominations().size());

		for (final LocalDistrict district : getElection().getDistrict().getChildren()) {
			final Map<LocalNomination, Integer> localVotes = votes.entrySet()
					.stream()
					.filter(entry -> entry.getKey().getDistrict().equals(district))
					.collect(toLinkedHashMap());

			// Result Type: Direct
			final Map<LocalNomination, LocalNominationResultType> localResultTypes
					= getDirectResults(localVotes).stream()
							.collect(toLinkedHashMap(identity(), nomination -> LocalNominationResultType.DIRECT));

			// Result Type: Direct Draw
			putDrawNominations(localResultTypes,
					localVotes,
					getDirectDrawResults(district),
					LocalNominationResultType.DIRECT,
					LocalNominationResultType.DIRECT_DRAW);

			resultTypes.putAll(localResultTypes);
		}

		// Result Type: List
		final Map<LocalNomination, BigDecimal> sainteLague = getSainteLague(votes, votesOfParties);
		for (final LocalNomination nomination : getListResults(votes, sainteLague)) {
			resultTypes.putIfAbsent(nomination, LocalNominationResultType.LIST);
		}

		// Result Type: List Draw
		putDrawNominations(resultTypes,
				sainteLague,
				getListDrawResults(),
				LocalNominationResultType.LIST,
				LocalNominationResultType.LIST_DRAW);

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

	/**
	 * Calculates the number of votes per nomination. The returned map is sorted by
	 * value (high to low) and nomination. Nominations, which were not voted for are
	 * not part of the result.
	 *
	 * @return the number of votes per nomination
	 */
	private Map<LocalNomination, Integer> getVotesOfNominations() {
		// Calculate
		final Map<LocalNomination, Integer> votes = getBallots().stream()
				.filter(Ballot::isValid)
				.map(LocalBallot::getNominations)
				.flatMap(Set::stream)
				.collect(toMap(identity(), nomination -> 1, (oldValue, thisValue) -> oldValue + thisValue));

		// Sort
		return Maps.sort(votes, VOTES_OF_NOMINATIONS_COMPARATOR);
	}

	/**
	 * Calculates the number of votes per party. The returned map is sorted by value
	 * (low to high) and party. Parties, which were not voted for are not part of
	 * the result.
	 *
	 * @return the number of votes per party
	 */
	private Map<Party, Integer> getVotesOfParties() {
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
		return Maps.sort(votes, VOTES_OF_PARTY_COMPARATOR);
	}

	/**
	 * Calculates the Sainte Laguë value for each nomination.
	 *
	 * @param votes          the number of votes per nomination
	 * @param votesOfParties the number of votes per party
	 * @return the Sainte Laguë value for each nomination
	 */
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
		// TODO: What's this sort order about? Can be eliminate
		// VOTES_BY_PARTY_COMPARATOR?
		final List<LocalNomination> nominations = getElection().getNominations();
		return Maps.sort(sainteLague,
				Comparator.<Entry<LocalNomination, BigDecimal>, BigDecimal>comparing(Entry::getValue)
						.thenComparing(entry -> votes.getOrDefault(entry.getKey(), 0))
						.reversed()
						.thenComparing(entry -> nominations.indexOf(entry.getKey())));
	}

	/**
	 * Calculates the Sainte Laguë value for each nomination of {@code party}.
	 *
	 * <p>
	 * The returned map is ordered by Sainte Laguë value (high to low).
	 *
	 * @param votes        the number of votes per nomination
	 * @param party        the party
	 * @param votesOfParty the number of votes for {@code party}
	 * @return the Sainte Laguë value for each nomination of {@code party}
	 */
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

	/**
	 * Returns a set of all nominations of {@code party} in their order of Sainte
	 * Laguë value assignment.
	 *
	 * <p>
	 * Directly elected nominations of {@code party} come first in order of their
	 * number of votes (high to low). All other nominations of {@code party} follow
	 * in given order.
	 *
	 * @param votes the number of votes per nomination
	 * @param party the party
	 * @return the nominations of {@code party} in their order of Sainte Laguë value
	 *         assignment
	 */
	private Set<LocalNomination> getSainteLagueNominationsOfParty(final Map<LocalNomination, Integer> votes,
			final Party party) {
		final Set<LocalNomination> nominations = new LinkedHashSet<>();

		// Result Type: Direct and Direct Draw
		getDirectResults(votes).stream()
				.filter(nomination -> nomination.getParty().map(party::equals).orElse(Boolean.FALSE))
				.forEach(nominations::add);

		// others
		nominations.addAll(getElection().getNominationsOfParty(party));

		return nominations;
	}

	/**
	 * Calculates the Sainte Laguë value using the formula:
	 *
	 * <p>
	 * {@code votesOfParty / (step + 0.5)}
	 *
	 * <p>
	 * {@code step} starts at zero and the scale of the returned {@link BigDecimal}
	 * is given by {@link LocalElection#getSainteLagueScale()}.
	 *
	 * @param votesOfParty the number of votes of the corresponding party
	 * @param step         step, starting at zero
	 * @return the Sainte Laguë value
	 */
	@SuppressWarnings("checkstyle:MagicNumber")
	private BigDecimal getSainteLagueValue(final BigDecimal votesOfParty, final int step) {
		return votesOfParty.divide(BigDecimal.valueOf(step * 10L + 5, 1),
				getElection().getSainteLagueScale(),
				RoundingMode.HALF_UP);
	}

	/**
	 * Returns a set of nominations, which were elected directly.
	 *
	 * <p>
	 * Implementation notice: This method simply returns the best few nominations
	 * based on {@link LocalElection#getNumberOfDirectSeatsPerLocalDistrict()}.
	 * Therefore there is a chance, that some of the returned nominations need to
	 * turn into direct draw candidates.
	 *
	 * @param votes the number of votes per nomination
	 * @return the directly elected nominations
	 */
	private Set<LocalNomination> getDirectResults(final Map<LocalNomination, Integer> votes) {
		return votes.keySet()
				.stream()
				.limit(getElection().getNumberOfDirectSeatsPerLocalDistrict())
				.collect(toLinkedHashSet());
	}

	/**
	 * Puts nominations with either draw results or open draws to
	 * {@code resultTypes}.
	 *
	 * <p>
	 * The number of votes of a draw are given by the last nomination of
	 * {@code resultTypes} and the number of maximum possible seats is given by the
	 * size of {@code resultTypes}.
	 *
	 * <p>
	 * {@code drawResults} must neither contain more nominations than possible draw
	 * seats, nor invalid nominations. In addition it does not need to contain a
	 * draw result for all open draws or even any draw result. In such cases one of
	 * the draw {@link LocalNominationResultType} is put to {@code resultTypes}
	 * according to {@code currentResultTypeDraw}.
	 *
	 * @param resultTypes           result types per nomination
	 * @param votes                 the number of votes per nomination
	 * @param drawResults           optional draws results to take into account
	 * @param currentResultType     the original result type, which might need draws
	 * @param currentResultTypeDraw the corresponding draw result type
	 */
	private void putDrawNominations(final Map<LocalNomination, LocalNominationResultType> resultTypes,
			final Map<LocalNomination, ? extends Number> votes,
			final Set<LocalNomination> drawResults,
			final LocalNominationResultType currentResultType,
			final LocalNominationResultType currentResultTypeDraw) {
		final Optional<LocalNomination> lastNomination
				= resultTypes.keySet().stream().reduce((first, second) -> second);
		if (!lastNomination.isPresent()) {
			return;
		}

		final Number votesForLastNomination = votes.get(lastNomination.get());
		final int numberOfPossibleSeats = resultTypes.size();

		// Remove the last few entries, which have the same result type and votes
		resultTypes.entrySet()
				.removeIf(entry -> entry.getValue() == currentResultType
						&& votes.get(entry.getKey()).equals(votesForLastNomination));
		final int numberOfDrawSeats = numberOfPossibleSeats - resultTypes.size();

		// Add positive draw results
		validateDrawResults(resultTypes,
				votes,
				votesForLastNomination,
				numberOfDrawSeats,
				drawResults,
				currentResultType);
		for (final LocalNomination drawResult : drawResults) {
			resultTypes.put(drawResult, currentResultType);
		}

		// Add open draws
		final Set<LocalNomination> drawNominations = getDrawNominations(resultTypes,
				votes,
				votesForLastNomination,
				numberOfDrawSeats,
				drawResults,
				currentResultType);
		final LocalNominationResultType drawResultType
				= drawNominations.size() > numberOfDrawSeats ? currentResultTypeDraw : currentResultType;
		for (final LocalNomination nomination : drawNominations) {
			resultTypes.putIfAbsent(nomination, drawResultType);
		}
	}

	/**
	 * Validates {@code drawResults}.
	 *
	 * <p>
	 * Right now the number of draw results must not be larger than the number of
	 * draw seats. In addition each nomination must be part of the draw by number of
	 * votes and result type.
	 *
	 * @param resultTypes            result types per nomination
	 * @param votes                  the number of votes per nomination
	 * @param votesForLastNomination the number of votes for nominations of the draw
	 * @param numberOfDrawSeats      the number of draw seats
	 * @param drawResults            optional draws results to take into account
	 * @param currentResultType      the original result type, which might need
	 *                               draws
	 */
	private void validateDrawResults(final Map<LocalNomination, LocalNominationResultType> resultTypes,
			final Map<LocalNomination, ? extends Number> votes,
			final Number votesForLastNomination,
			final int numberOfDrawSeats,
			final Set<LocalNomination> drawResults,
			final LocalNominationResultType currentResultType) {
		if (drawResults.size() > numberOfDrawSeats) {
			throw new ElectionException("%d %s draw results given while expecting %d %s draw results at max.",
					drawResults.size(),
					currentResultType.toString().toLowerCase(Locale.ROOT),
					numberOfDrawSeats,
					currentResultType.toString().toLowerCase(Locale.ROOT));
		}

		for (final LocalNomination nomination : drawResults) {
			if (!Nullables.orElse(votes.get(nomination), 0).equals(votesForLastNomination)
					|| resultTypes.getOrDefault(nomination, currentResultType) != currentResultType) {
				throw new ElectionException("\"%s\" is not part of the %s draw.",
						nomination.getPerson().getKey(),
						currentResultType.toString().toLowerCase(Locale.ROOT));
			}
		}
	}

	/**
	 * Returns the nominations, which are part of the draw.
	 *
	 * @param resultTypes            result types per nomination
	 * @param votes                  the number of votes per nomination
	 * @param votesForLastNomination the number of votes for nominations of the draw
	 * @param numberOfDrawSeats      the number of draw seats
	 * @param drawResults            optional draws results to take into account
	 * @param currentResultType      the original result type, which might need
	 *                               draws
	 * @return the nominations, which are part of the draw
	 */
	private Set<LocalNomination> getDrawNominations(final Map<LocalNomination, LocalNominationResultType> resultTypes,
			final Map<LocalNomination, ? extends Number> votes,
			final Number votesForLastNomination,
			final int numberOfDrawSeats,
			final Set<LocalNomination> drawResults,
			final LocalNominationResultType currentResultType) {
		// No need to iterate "votes" if all draw seats are filled with draw results.
		if (numberOfDrawSeats <= drawResults.size()) {
			return emptySet();
		}

		return votes.entrySet()
				.stream()
				.filter(entry -> entry.getValue().equals(votesForLastNomination))
				.map(Entry::getKey)
				.filter(nomination -> resultTypes.getOrDefault(nomination, currentResultType) == currentResultType)
				.collect(toSet());
	}

	/**
	 * Returns a set of nominations, which were elected by list.
	 *
	 * <p>
	 * Implementation notice: This method simply returns the best few nominations
	 * based on {@link LocalElection#getNumberOfSeats()}. Therefore there is a
	 * chance, that some of the returned nominations need to turn into list draw
	 * candidates.
	 *
	 * <p>
	 * In case some direct results have a relatively low Sainte Laguë value the
	 * returned map might be larger than {@link LocalElection#getNumberOfSeats()}.
	 *
	 * @param votes       the number of votes per nomination
	 * @param sainteLague the Sainte Laguë value per nomination
	 * @return the elected nominations by list
	 */
	private Set<LocalNomination> getListResults(final Map<LocalNomination, Integer> votes,
			final Map<LocalNomination, BigDecimal> sainteLague) {
		final Set<LocalNomination> directNominationsWithParty = getDirectResults(votes).stream()
				.filter(nomination -> nomination.getParty().isPresent())
				.collect(toSet());
		final int numberOfSeats = getElection().getNumberOfSeats();

		final Set<LocalNomination> nominations = new LinkedHashSet<>(numberOfSeats);
		for (final LocalNomination nomination : sainteLague.keySet()) {
			if (nominations.size() >= numberOfSeats && nominations.containsAll(directNominationsWithParty)) {
				return nominations;
			}
			nominations.add(nomination);
		}
		return nominations;
	}

	/**
	 * Returns nominations, which have either balance or overhang seats.
	 *
	 * @param sainteLague the Sainte Laguë value per nomination
	 * @param resultTypes result types per nomination
	 * @return the nominations, which have either balance or overhang seats
	 */
	private Set<LocalNomination> getBalanceAndOverhangSeats(final Map<LocalNomination, BigDecimal> sainteLague,
			final Map<LocalNomination, LocalNominationResultType> resultTypes) {
		return sainteLague.keySet()
				.stream()
				.limit(resultTypes.size())
				.skip(getElection().getNumberOfSeats())
				.collect(toSet());
	}

	/**
	 * Creates the party results.
	 *
	 * <p>
	 * This method is used within the method constructor. Calculating the results is
	 * done within {@link LocalPartyResult} and required the nomination results to
	 * be calculated beforehand.
	 *
	 * @return Wahlergebnis einzelner politischer Parteien und Wählergruppen
	 */
	private Map<Party, LocalPartyResult> createPartyResults() {
		return getElection().getParties()
				.stream()
				.map(party -> new LocalPartyResult(this, party))
				.sorted()
				.collect(toLinkedHashMap(PartyResult::getParty, identity()));
	}

	/**
	 * JSON delegate for {@link LocalElectionResult}
	 */
	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalElectionResult {
		/**
		 * Optional number of all ballots of the election
		 *
		 * <p>
		 * In case this number is larger than the size of the list of ballots, then some
		 * ballots were not evaluated, yet.
		 *
		 * @return the number of all ballots of the election or empty
		 */
		OptionalInt numberOfAllBallots;

		/**
		 * Stimmzettel
		 */
		List<ParsableLocalBallot> ballots;

		/**
		 * Ausgeloste Loskandidaten mit Direktmandat
		 */
		Set<ParsableLocalNomination> directDrawResults;

		/**
		 * Ausgeloste Loskandidaten mit Listenmandat
		 */
		Set<ParsableLocalNomination> listDrawResults;

		/**
		 * Stimmzettel
		 *
		 * @return Stimmzettel
		 */
		public List<LocalBallot> getLocalBallots() {
			final LocalElection election = ELECTION_FOR_JSON_CREATOR.get();
			return ballots.stream()
					.map(ballot -> ballot.isValid()
							? LocalBallot.createValidBallot(election,
									ballot.getPollingStation(),
									ballot.isPostalVote(),
									ballot.getNominations())
							: LocalBallot
									.createInvalidBallot(election, ballot.getPollingStation(), ballot.isPostalVote()))
					.collect(toList());
		}

		/**
		 * Ausgeloste Loskandidaten mit Direktmandat
		 *
		 * @return Ausgeloste Loskandidaten mit Direktmandat
		 */
		public Set<LocalNomination> getDirectDrawResults() {
			return ParsableLocalNomination.convert(ELECTION_FOR_JSON_CREATOR.get(), directDrawResults);
		}

		/**
		 * Ausgeloste Loskandidaten mit Listenmandat
		 *
		 * @return Ausgeloste Loskandidaten mit Listenmandat
		 */
		public Set<LocalNomination> getListDrawResults() {
			return ParsableLocalNomination.convert(ELECTION_FOR_JSON_CREATOR.get(), listDrawResults);
		}
	}

	/**
	 * JSON delegate for {@link LocalBallot}
	 */
	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalBallot {
		/**
		 * Wahlbezirk
		 */
		String pollingStation;

		/**
		 * Briefwahl
		 *
		 * @return {@code true} for postal vote ballots, else {@code false}
		 */
		boolean postalVote;

		/**
		 * Ungültige Stimme
		 *
		 * @return {@code true} for valid ballots, else {@code false}
		 */
		boolean valid;

		/**
		 * Gewählte Bewerberinnen und Bewerber
		 */
		Set<ParsableLocalNomination> nominations;

		/**
		 * Wahlbezirk
		 *
		 * @return Wahlbezirk
		 */
		public LocalPollingStation getPollingStation() {
			return ELECTION_FOR_JSON_CREATOR.get()
					.getDistrict()
					.getChildren()
					.stream()
					.map(LocalDistrict::getChildren)
					.flatMap(Collection::stream)
					.filter(district -> pollingStation.equals(district.getKey()))
					.findAny()
					.orElseThrow(() -> new ElectionException(
							"Could not find polling station with key \"%s\" for election \"%s\".",
							getPollingStation(),
							ELECTION_FOR_JSON_CREATOR.get().getName()));
		}

		/**
		 * Gewählte Bewerberinnen und Bewerber
		 *
		 * @return Gewählte Bewerberinnen und Bewerber
		 */
		public Set<LocalNomination> getNominations() {
			return ParsableLocalNomination.convert(ELECTION_FOR_JSON_CREATOR.get(), nominations);
		}
	}
}
