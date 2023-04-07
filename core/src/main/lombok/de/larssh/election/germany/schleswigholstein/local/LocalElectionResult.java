package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toLinkedHashMap;
import static de.larssh.utils.Collectors.toLinkedHashSet;
import static de.larssh.utils.Collectors.toMap;
import static java.util.Collections.emptyMap;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.DistrictValueMap;
import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.ElectionResult;
import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.PartyResult;
import de.larssh.election.utils.BigDecimals;
import de.larssh.utils.Nullables;
import de.larssh.utils.OptionalInts;
import de.larssh.utils.Optionals;
import de.larssh.utils.annotations.PackagePrivate;
import de.larssh.utils.collection.Maps;
import de.larssh.utils.text.Strings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Wahlergebnis auf Basis einer ggf. gefilterten Liste an Stimmzetteln
 */
@Getter
@ToString
@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.DataClass", "PMD.ExcessiveImports", "PMD.GodClass" })
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
	@EqualsAndHashCode.Include
	LocalElection election;

	/**
	 * Scale (decimal places) of Sainte Laguë values
	 *
	 * <p>
	 * Usually this is {@code 2}.
	 *
	 * @return the scale (decimal places) of Sainte Laguë values
	 */
	@JsonProperty(access = Access.READ_ONLY, index = 0)
	int sainteLagueScale;

	/**
	 * Anzahl aller Stimmzettel nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 *
	 * <p>
	 * In case this number is larger than the size of the list of ballots for the
	 * district, not all ballots were evaluated, yet.
	 */
	@ToString.Exclude
	@Getter(AccessLevel.NONE)
	@EqualsAndHashCode.Include
	@JsonProperty(access = Access.READ_ONLY, index = 1)
	DistrictValueMap numberOfAllBallots;

	/**
	 * Ausgeloste Loskandidaten mit Direktmandat
	 *
	 * @return Ausgeloste Loskandidaten mit Direktmandat
	 */
	@ToString.Exclude
	@EqualsAndHashCode.Include
	Set<LocalNomination> directDrawResults;

	/**
	 * Ausgeloste Loskandidaten mit Listenmandat
	 *
	 * @return Ausgeloste Loskandidaten mit Listenmandat
	 */
	@ToString.Exclude
	@EqualsAndHashCode.Include
	Set<LocalNomination> listDrawResults;

	/**
	 * Stimmzettel
	 *
	 * @return Stimmzettel
	 */
	@ToString.Exclude
	List<LocalBallot> ballots;

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
	 * Wahlergebnis
	 *
	 * @param parsable JSON delegate
	 */
	@JsonCreator(mode = Mode.DELEGATING)
	private LocalElectionResult(final ParsableLocalElectionResult parsable) {
		this(ELECTION_FOR_JSON_CREATOR.get(),
				parsable.getSainteLagueScale(),
				parsable.getNumberOfAllBallots(),
				parsable.getDirectDrawResults(),
				parsable.getListDrawResults(),
				parsable.getLocalBallots());
	}

	/**
	 * Wahlergebnis
	 *
	 * @param election           Wahl
	 * @param sainteLagueScale   Scale (decimal places) of Sainte Laguë values
	 * @param numberOfAllBallots optional number of all ballots of the election
	 * @param directDrawResults  Ausgeloste Loskandidaten mit Direktmandat
	 * @param listDrawResults    Ausgeloste Loskandidaten mit Listenmandat
	 * @param ballots            Stimmzettel
	 */
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Election is no longer modifiable when passed here.")
	public LocalElectionResult(final LocalElection election,
			final int sainteLagueScale,
			final Map<District<?>, OptionalInt> numberOfAllBallots,
			final Set<LocalNomination> directDrawResults,
			final Set<LocalNomination> listDrawResults,
			final List<LocalBallot> ballots) {
		this.election = election;
		this.sainteLagueScale = sainteLagueScale;
		this.directDrawResults = unmodifiableSet(new LinkedHashSet<>(directDrawResults));
		this.listDrawResults = unmodifiableSet(new LinkedHashSet<>(listDrawResults));
		this.ballots = unmodifiableList(ballots);

		this.numberOfAllBallots = new DistrictValueMap(election);
		this.numberOfAllBallots.putAll(numberOfAllBallots);

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

	/**
	 * Creates a new {@link LocalElectionResult} by merging {@code this} and
	 * multiple {@code results} of the same election.
	 *
	 * @param resultsToAdd results to merge with {@code this}
	 * @return a new result object with the information of {@code this} and all
	 *         {@code resultsToAdd}
	 */
	public LocalElectionResult add(final LocalElectionResult... resultsToAdd) {
		if (!Arrays.stream(resultsToAdd).allMatch(result -> result.getElection() == getElection())) {
			throw new IllegalArgumentException("Election results of a different election cannot be merged.");
		}

		// Create a new set including this
		final Set<LocalElectionResult> results = new LinkedHashSet<>();
		results.add(this);
		results.addAll(Arrays.asList(resultsToAdd));

		// Calculate number of all ballots
		final Map<District<?>, OptionalInt> numberOfAllBallots = new HashMap<>();
		for (final District<?> district : getElection().getAllDistricts()) {
			Optionals.ofSingle(results.stream()
					.map(result -> result.getNumberOfAllBallots(district))
					.filter(OptionalInt::isPresent)
					.distinct()).ifPresent(value -> numberOfAllBallots.put(district, value));
		}

		final Set<LocalNomination> directDrawResults = results.stream()
				.map(LocalElectionResult::getDirectDrawResults)
				.flatMap(Collection::stream)
				.collect(toSet());
		final Set<LocalNomination> listDrawResults = results.stream()
				.map(LocalElectionResult::getListDrawResults)
				.flatMap(Collection::stream)
				.collect(toSet());
		final List<LocalBallot> ballots
				= results.stream().map(LocalElectionResult::getBallots).flatMap(Collection::stream).collect(toList());
		return new LocalElectionResult(getElection(),
				sainteLagueScale,
				numberOfAllBallots,
				directDrawResults,
				listDrawResults,
				ballots);
	}

	/** {@inheritDoc} */
	@Override
	public LocalElectionResult filter(final Predicate<? super LocalBallot> filter) {
		final List<LocalBallot> filteredBallots = getBallots().stream().filter(filter).collect(toList());

		return new LocalElectionResult(getElection(),
				sainteLagueScale,
				emptyMap(),
				getDirectDrawResults(),
				getListDrawResults(),
				filteredBallots);
	}

	/**
	 * Filters the current list of ballots by {@code district} and returns a new
	 * {@link ElectionResult}
	 *
	 * @param district the district to filter for
	 * @return a new {@link ElectionResult} with filtered ballots
	 */
	public LocalElectionResult filterByDistrict(final District<?> district) {
		return new LocalElectionResult(getElection(),
				sainteLagueScale,
				numberOfAllBallots,
				getDirectDrawResults(),
				getListDrawResults(),
				getBallots(district));
	}

	/**
	 * Stimmzettel eines Wahlgebiets, Wahlkreises oder Wahlbezirks
	 *
	 * @param district the district to filter for
	 * @return Stimmzettel
	 */
	public List<LocalBallot> getBallots(final District<?> district) {
		return ballots.stream().filter(ballot -> district.contains(ballot.getPollingStation())).collect(toList());
	}

	/**
	 * Stimmzettel
	 *
	 * @return Stimmzettel
	 */
	@EqualsAndHashCode.Include
	private List<LocalBallot> getBallotsForEqualsAndHashCode() {
		return ballots.stream().sorted().collect(toList());
	}

	/**
	 * Calculates the ballot evaluation progress in percentage.
	 *
	 * @param scale the scale of the {@link BigDecimal} quotient
	 * @return the ballot evaluation progress in percentage or empty if the number
	 *         of all ballots is empty
	 */
	public Optional<BigDecimal> getEvaluationProgress(final int scale) {
		return getEvaluationProgress(scale, getElection().getDistrict());
	}

	/**
	 * Calculates the ballot evaluation progress of any district in percentage.
	 *
	 * @param scale    the scale of the {@link BigDecimal} quotient
	 * @param district the district to filter for
	 * @return the ballot evaluation progress in percentage or empty if the number
	 *         of all ballots is empty
	 */
	@SuppressWarnings("checkstyle:MagicNumber")
	public Optional<BigDecimal> getEvaluationProgress(final int scale, final District<?> district) {
		return OptionalInts
				.mapToObj(getNumberOfAllBallots(district),
						numberOfAllBallots -> BigDecimals
								.divideOrZero(100L * getBallots(district).size(), numberOfAllBallots, scale))
				.map(BigDecimal.valueOf(100).setScale(scale)::min);
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

	/** {@inheritDoc} */
	@Override
	public OptionalInt getNumberOfAllBallots() {
		return getNumberOfAllBallots(getElection().getDistrict());
	}

	/**
	 * Anzahl aller Stimmzettel nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 *
	 * <p>
	 * In case this number is larger than the size of the list of ballots for the
	 * district, not all ballots were evaluated, yet.
	 *
	 * @param district Wahlgebiet, Wahlkreis oder Wahlbezirk
	 * @return Anzahl aller Stimmzettel nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 */
	public OptionalInt getNumberOfAllBallots(final District<?> district) {
		return numberOfAllBallots.get(district);
	}

	/**
	 * Anzahl aller Stimmzettel nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 *
	 * <p>
	 * In case this number is larger than the size of the list of ballots for the
	 * district, not all ballots were evaluated, yet.
	 *
	 * @return Anzahl aller Stimmzettel nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 */
	@JsonIgnore
	public Map<District<?>, OptionalInt> getNumberOfAllBallotsMap() {
		return unmodifiableMap(numberOfAllBallots);
	}

	/**
	 * Number of invalid ballots
	 *
	 * @return the number of invalid ballots
	 */
	@JsonIgnore
	public int getNumberOfInvalidBallots() {
		return getNumberOfInvalidBallots(getElection().getDistrict());
	}

	/**
	 * Anzahl ungültiger Stimmzettel nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 *
	 * @param district Wahlgebiet, Wahlkreis oder Wahlbezirk
	 * @return the number of invalid ballots
	 */
	public int getNumberOfInvalidBallots(final District<?> district) {
		return (int) getBallots(district).stream().filter(ballot -> !ballot.isValid()).count();
	}

	/**
	 * Number of postal ballots
	 *
	 * @return the number of postal ballots
	 */
	@JsonIgnore
	public int getNumberOfPostalBallots() {
		return (int) getBallots().stream().filter(LocalBallot::isPostalVote).count();
	}

	/**
	 * Number of votes
	 *
	 * @return the number of votes
	 */
	@JsonIgnore
	public int getNumberOfVotes() {
		return getNumberOfVotes(getElection().getDistrict());
	}

	/**
	 * Anzahl der Stimmen nach Wahlgebiet, Wahlkreis oder Wahlbezirk
	 *
	 * @param district Wahlgebiet, Wahlkreis oder Wahlbezirk
	 * @return the number of votes
	 */
	public int getNumberOfVotes(final District<?> district) {
		return getBallots(district).stream()
				.filter(LocalBallot::isValid)
				.map(LocalBallot::getNominations)
				.mapToInt(Set::size)
				.sum();
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
		final Map<LocalNomination, LocalNominationResultType> resultTypes
				= new LinkedHashMap<>(getElection().getNominations().size());
		final Set<LocalNomination> directDrawNominations = new HashSet<>();

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
			directDrawNominations.addAll(getDrawResultsAndUpdate(localResultTypes,
					localVotes,
					getDirectDrawResults(district),
					LocalNominationResultType.DIRECT));
			resultTypes.putAll(localResultTypes);
		}

		// Result Type: Direct Balance Seat
		final Map<LocalNomination, BigDecimal> sainteLague = getSainteLague(votes);
		for (final LocalNomination nomination : getBalanceSeats(resultTypes, sainteLague)) {
			resultTypes.put(nomination, LocalNominationResultType.DIRECT_BALANCE_SEAT);
		}

		// Result Type: List
		for (final LocalNomination nomination : getListResults(resultTypes, sainteLague, directDrawNominations)) {
			resultTypes.putIfAbsent(nomination, LocalNominationResultType.LIST);
		}

		// Result Type: List Draw
		for (final LocalNomination nomination : getDrawResultsAndUpdate(resultTypes,
				sainteLague,
				getListDrawResults(),
				LocalNominationResultType.LIST)) {
			resultTypes.put(nomination, LocalNominationResultType.LIST_DRAW);
		}

		// Result Type: Direct Draw List
		resultTypes.putAll(getDirectDrawListResults(resultTypes, directDrawNominations));

		// Result Type: List Overhang Seat
		for (final LocalNomination nomination : getOverhangSeats(resultTypes, votes, sainteLague)) {
			resultTypes.put(nomination, LocalNominationResultType.LIST_OVERHANG_SEAT);
		}

		// Result Type: Not Elected
		return getElection().getNominations()
				.stream()
				.map(nomination -> new LocalNominationResult(this,
						nomination,
						resultTypes.getOrDefault(nomination, LocalNominationResultType.NOT_ELECTED),
						Optional.ofNullable(sainteLague.get(nomination))))
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
	 * Returns a set of nominations for open draws, removes those from
	 * {@code resultTypes} and puts {@code currentResultType} for closed draws.
	 *
	 * <p>
	 * The number of e.g. votes of a draw are given by the last nomination of
	 * {@code resultTypes} and the number of maximum possible seats is given by the
	 * size of {@code resultTypes}.
	 *
	 * <p>
	 * {@code drawResults} must neither contain more nominations than possible draw
	 * seats, nor invalid nominations. In addition it does not need to contain a
	 * draw result for all open draws or any draw result.
	 *
	 * @param resultTypes       result types per nomination
	 * @param values            the number of e.g. votes per nomination
	 * @param drawResults       optional draws results to take into account
	 * @param currentResultType the original result type, which might need draws
	 * @return the nominations with an open draw
	 */
	private Set<LocalNomination> getDrawResultsAndUpdate(
			final Map<LocalNomination, LocalNominationResultType> resultTypes,
			final Map<LocalNomination, ? extends Number> values,
			final Set<LocalNomination> drawResults,
			final LocalNominationResultType currentResultType) {
		final Optional<LocalNomination> lastResult = resultTypes.entrySet()
				.stream()
				.filter(entry -> entry.getValue() == currentResultType)
				.map(Entry::getKey)
				.reduce((first, second) -> second);
		if (!lastResult.isPresent()) {
			return emptySet();
		}

		// Identifying all nominations, that might be affected by the draw according to
		// their value
		final Number valueOfLastNomination = values.get(lastResult.get());
		final Set<LocalNomination> affectedNominations = values.entrySet()
				.stream()
				.filter(entry -> entry.getValue().equals(valueOfLastNomination)
						&& resultTypes.getOrDefault(entry.getKey(), currentResultType) == currentResultType)
				.map(Entry::getKey)
				.collect(toSet());

		// If all affected nominations are part of "resultType" there is no draw needed.
		if (affectedNominations.stream().map(resultTypes::get).allMatch(currentResultType::equals)) {
			return emptySet();
		}

		// Removing the affected nominations to start from scratch
		final int numberOfPossibleSeats = resultTypes.size();
		resultTypes.keySet().removeAll(affectedNominations);

		// Add positive draw results
		validateDrawResults(affectedNominations,
				values,
				valueOfLastNomination,
				numberOfPossibleSeats - resultTypes.size(),
				drawResults,
				currentResultType);
		for (final LocalNomination drawResult : drawResults) {
			resultTypes.put(drawResult, currentResultType);
			affectedNominations.remove(drawResult);
		}

		// Add open draws
		return numberOfPossibleSeats > resultTypes.size() ? affectedNominations : emptySet();
	}

	/**
	 * Validates {@code drawResults}.
	 *
	 * <p>
	 * Right now the number of draw results must not be larger than the number of
	 * draw seats. In addition each nomination must be part of the draw by value and
	 * nomination.
	 *
	 * @param affectedNominations   set of affected nominations
	 * @param values                the number of e.g. votes per nomination
	 * @param valueOfLastNomination the number of e.g. votes of nominations of the
	 *                              draw
	 * @param numberOfDrawSeats     the number of draw seats
	 * @param drawResults           optional draws results to take into account
	 * @param currentResultType     the original result type, which might need draws
	 */
	private void validateDrawResults(final Set<LocalNomination> affectedNominations,
			final Map<LocalNomination, ? extends Number> values,
			final Number valueOfLastNomination,
			final int numberOfDrawSeats,
			final Set<LocalNomination> drawResults,
			final LocalNominationResultType currentResultType) {
		if (drawResults.size() > numberOfDrawSeats) {
			throw new ElectionException("%d %s draw results given while expecting %d %s draw results at max.",
					drawResults.size(),
					Strings.toLowerCaseNeutral(currentResultType.toString()),
					numberOfDrawSeats,
					Strings.toLowerCaseNeutral(currentResultType.toString()));
		}

		for (final LocalNomination nomination : drawResults) {
			if (!Nullables.orElse(values.get(nomination), 0).equals(valueOfLastNomination)
					|| !affectedNominations.contains(nomination)) {
				throw new ElectionException("\"%s\" is not part of the %s draw.",
						nomination.getPerson().getKey(),
						Strings.toLowerCaseNeutral(currentResultType.toString()));
			}
		}
	}

	/**
	 * Calculates the Sainte Laguë value for each nomination.
	 *
	 * <p>
	 * The returned map is ordered by Sainte Laguë value (high to low) and the
	 * number of votes (high to lower).
	 *
	 * @param votes the number of votes per nomination
	 * @return the Sainte Laguë value for each nomination
	 */
	private Map<LocalNomination, BigDecimal> getSainteLague(final Map<LocalNomination, Integer> votes) {
		// Calculate
		final Map<LocalNomination, BigDecimal> sainteLague = getVotesOfParties().entrySet()
				.stream()
				.map(entry -> getSainteLagueOfParty(votes, entry.getKey(), entry.getValue()))
				.map(Map::entrySet)
				.flatMap(Set::stream)
				.collect(toLinkedHashMap());

		// Sort
		return Maps.sort(sainteLague,
				Comparator.<Entry<LocalNomination, BigDecimal>, BigDecimal>comparing(Entry::getValue)
						.thenComparing(entry -> votes.getOrDefault(entry.getKey(), 0))
						.reversed()
						.thenComparing(Entry::getKey));
	}

	/**
	 * Calculates the number of votes per party. The returned map is sorted by value
	 * (low to high) and party. Parties, which were not voted for are not part of
	 * the result.
	 *
	 * @return the number of votes per party
	 */
	private Map<Party, Integer> getVotesOfParties() {
		return getBallots().stream()
				.filter(Ballot::isValid)
				.map(Ballot::getNominations)
				.flatMap(Set::stream)
				.map(Nomination::getParty)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toMap(identity(), party -> 1, (oldValue, thisValue) -> oldValue + thisValue));
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
		final Iterator<LocalNomination> iterator = nominationsOfParty.iterator();

		return IntStream.range(0, nominationsOfParty.size())
				.mapToObj(step -> getSainteLagueValue(votesOfParty, step))
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
		nominations.addAll(getElection().getListNominations(party));

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
	private BigDecimal getSainteLagueValue(final int votesOfParty, final int step) {
		return BigDecimals.divide(votesOfParty, BigDecimal.valueOf(step * 10 + 5, 1), getSainteLagueScale());
	}

	/**
	 * Returns nominations, which have balance seats.
	 *
	 * @param resultTypes result types per nomination
	 * @param sainteLague the Sainte Laguë value per nomination
	 * @return the nominations, which have balance seats
	 */
	private Set<LocalNomination> getBalanceSeats(final Map<LocalNomination, LocalNominationResultType> resultTypes,
			final Map<LocalNomination, BigDecimal> sainteLague) {
		return sainteLague.keySet()
				.stream()
				.skip(getElection().getNumberOfSeats())
				.filter(nomination -> resultTypes.get(nomination) == LocalNominationResultType.DIRECT)
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
	 * In case some direct results have a relative low Sainte Laguë value the
	 * returned map might be larger than {@link LocalElection#getNumberOfSeats()},
	 * causing balance seats.
	 *
	 * <p>
	 * The returned map is ordered the same as {@code sainteLague}.
	 *
	 * @param resultTypes           result types per nomination
	 * @param sainteLague           the Sainte Laguë value per nomination
	 * @param directDrawNominations direct draw nominations
	 * @return the elected nominations by list
	 */
	private Set<LocalNomination> getListResults(final Map<LocalNomination, LocalNominationResultType> resultTypes,
			final Map<LocalNomination, BigDecimal> sainteLague,
			final Set<LocalNomination> directDrawNominations) {
		final Set<LocalNomination> directResults = resultTypes.entrySet()
				.stream()
				.filter(entry -> entry.getValue() == LocalNominationResultType.DIRECT
						|| entry.getValue() == LocalNominationResultType.DIRECT_BALANCE_SEAT)
				.map(Entry::getKey)
				.collect(toSet());
		final Set<LocalNomination> directResultsWithParty
				= directResults.stream().filter(nomination -> nomination.getParty().isPresent()).collect(toSet());
		final Set<LocalNomination> directDrawResultsWithParty = directDrawNominations.stream()
				.filter(nomination -> nomination.getParty().isPresent())
				.collect(toSet());

		final int numberOfSeats = getElection().getNumberOfSeats();
		final int numberOfDirectDrawSeats = getElection().getNumberOfDirectSeats() - directResults.size();

		final Set<LocalNomination> listResults = new LinkedHashSet<>(numberOfSeats);
		for (final Entry<LocalNomination, BigDecimal> entry : sainteLague.entrySet()) {
			if (listResults.size() >= numberOfSeats
					&& listResults.containsAll(directResultsWithParty)
					&& directDrawResultsWithParty.stream()
							.filter(listResults::contains)
							.count() >= numberOfDirectDrawSeats) {
				return listResults;
			}
			listResults.add(entry.getKey());
		}
		return listResults;
	}

	/**
	 * Returns a map of the direct draw nominations and their result type. If a
	 * direct draw result is also a list result
	 * {@link LocalNominationResultType#DIRECT_DRAW_LIST} is used, else
	 * {@link LocalNominationResultType#DIRECT_DRAW}.
	 *
	 * @param resultTypes           result types per nomination
	 * @param directDrawNominations direct draw nominations
	 * @return the direct draw nominations and their result type
	 */
	private Map<LocalNomination, LocalNominationResultType> getDirectDrawListResults(
			final Map<LocalNomination, LocalNominationResultType> resultTypes,
			final Set<LocalNomination> directDrawNominations) {
		return directDrawNominations.stream()
				.collect(toMap(identity(),
						nomination -> resultTypes.get(nomination) == LocalNominationResultType.LIST
								? LocalNominationResultType.DIRECT_DRAW_LIST
								: LocalNominationResultType.DIRECT_DRAW));
	}

	/**
	 * Returns nominations, which have overhang seats.
	 *
	 * @param resultTypes result types per nomination
	 * @param votes       the number of votes per nomination
	 * @param sainteLague the Sainte Laguë value per nomination
	 * @return the nominations, which have overhang seats
	 */
	private Set<LocalNomination> getOverhangSeats(final Map<LocalNomination, LocalNominationResultType> resultTypes,
			final Map<LocalNomination, Integer> votes,
			final Map<LocalNomination, BigDecimal> sainteLague) {
		return sainteLague.keySet()
				.stream()
				.filter(nomination -> !getDirectResults(votes).contains(nomination))
				.skip(getElection().getNumberOfListSeats())
				.filter(nomination -> resultTypes.get(nomination) == LocalNominationResultType.LIST)
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
		 * Finds a set of {@link LocalNomination}s by their keys.
		 *
		 * @param nominationKeys the keys to find
		 * @return a set of {@link LocalNomination}s, found by their keys
		 */
		public static Set<LocalNomination> findNominations(final Set<String> nominationKeys) {
			final LocalElection election = ELECTION_FOR_JSON_CREATOR.get();
			return nominationKeys.stream()
					.map(nominationKey -> Optionals
							.ofSingle(election.getNominations()
									.stream()
									.filter(nomination -> nomination.getKey().equals(nominationKey)))
							.orElseThrow(() -> new ElectionException(
									"Could not find nomination with key \"%s\" for election \"%s\".",
									nominationKey,
									election.getName())))
					.collect(toLinkedHashSet());
		}

		/**
		 * Anzahl aller Stimmzettel nach Wahlgebiet, Wahlkreis oder Wahlbezirk
		 *
		 * <p>
		 * In case this number is larger than the size of the list of ballots for the
		 * district, not all ballots were evaluated, yet.
		 */
		Map<String, OptionalInt> numberOfAllBallots;

		/**
		 * Scale (decimal places) of Sainte Laguë values
		 *
		 * <p>
		 * Usually this is {@code 2}.
		 *
		 * @return the scale (decimal places) of Sainte Laguë values
		 */
		int sainteLagueScale;

		/**
		 * Ausgeloste Loskandidaten mit Direktmandat
		 */
		Set<String> directDrawResults;

		/**
		 * Ausgeloste Loskandidaten mit Listenmandat
		 */
		Set<String> listDrawResults;

		/**
		 * Stimmzettel
		 */
		List<ParsableLocalBallot> ballots;

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
			return findNominations(directDrawResults);
		}

		/**
		 * Ausgeloste Loskandidaten mit Listenmandat
		 *
		 * @return Ausgeloste Loskandidaten mit Listenmandat
		 */
		public Set<LocalNomination> getListDrawResults() {
			return findNominations(listDrawResults);
		}

		/**
		 * Anzahl aller Stimmzettel nach Wahlgebiet, Wahlkreis oder Wahlbezirk
		 *
		 * <p>
		 * In case this number is larger than the size of the list of ballots for the
		 * district, not all ballots were evaluated, yet.
		 *
		 * @return Anzahl aller Stimmzettel nach Wahlgebiet, Wahlkreis oder Wahlbezirk
		 */
		public Map<District<?>, OptionalInt> getNumberOfAllBallots() {
			return ELECTION_FOR_JSON_CREATOR.get()
					.getAllDistricts()
					.stream()
					.collect(toMap(identity(),
							district -> numberOfAllBallots.getOrDefault(district.getKey(), OptionalInt.empty())));
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
		Set<String> nominations;

		/**
		 * Wahlbezirk
		 *
		 * @return Wahlbezirk
		 */
		public LocalPollingStation getPollingStation() {
			return ELECTION_FOR_JSON_CREATOR.get()
					.getPollingStations()
					.stream()
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
			return ParsableLocalElectionResult.findNominations(nominations);
		}
	}
}
