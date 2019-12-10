package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toLinkedHashMap;
import static de.larssh.utils.Collectors.toLinkedHashSet;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.ElectionResult;
import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.utils.annotations.PackagePrivate;
import de.larssh.utils.collection.Maps;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LocalElectionResult implements ElectionResult<LocalBallot> {
	LocalElection election;

	List<LocalBallot> ballots;

	Set<LocalNominationResult> nominationResults;

	Set<LocalPartyResult> partyResults;

	@PackagePrivate
	LocalElectionResult(final LocalElection election, final List<LocalBallot> ballots) {
		this(election, ballots, isEqual(true));
	}

	private LocalElectionResult(final LocalElection election,
			final List<LocalBallot> ballots,
			final Predicate<LocalBallot> filter) {
		this.election = election;
		this.ballots = unmodifiableList(ballots.stream().filter(filter).collect(toList()));
		nominationResults = unmodifiableSet(createNominationResults());
		partyResults = unmodifiableSet(createPartyResults());
	}

	@Override
	public ElectionResult<LocalBallot> filter(final Predicate<LocalBallot> filter) {
		return new LocalElectionResult(getElection(), getBallots(), filter);
	}

	public int getNumberOfVotes() {
		return (int) getBallots().stream()
				.filter(Ballot::isValid)
				.map(Ballot::getNominations)
				.flatMap(Collection::stream)
				.count();
	}

	public BigDecimal getTurnout(final int scale) {
		return BigDecimal.valueOf(getNumberOfVotes())
				.multiply(BigDecimal.TEN)
				.multiply(BigDecimal.TEN)
				.divide(BigDecimal.valueOf(getNumberOfVotes()), scale, RoundingMode.HALF_UP);
	}

	private Set<LocalNominationResult> createNominationResults() {
		final Map<LocalNomination, Integer> votes = getVotes();
		final Map<Party, Integer> votesOfParties = getVotesOfParty();

		// Result Type: Direct
		final Map<LocalNomination, LocalNominationResultType> resultTypes = new LinkedHashMap<>();
		getDirectNominations(votes)
				.forEach(nomination -> resultTypes.put(nomination, LocalNominationResultType.DIRECT));

		// Result Type: Direct Draw
		getDrawNominations(votes, resultTypes)
				.forEach(nomination -> resultTypes.put(nomination, LocalNominationResultType.DIRECT_DRAW));

		// Result Type: List
		final Map<LocalNomination, BigDecimal> sainteLague = getSainteLague(votes, votesOfParties);
		getListNominations(votes, sainteLague)
				.forEach(nomination -> resultTypes.putIfAbsent(nomination, LocalNominationResultType.LIST));

		// Result Type: Direct Balance Seat
		// TODO: Direct Balance Seat

		// Result Type: List Overhang Seat
		// TODO: List Overhang Seat

		// Result Type: List Draw
		getDrawNominations(sainteLague, resultTypes)
				.forEach(nomination -> resultTypes.put(nomination, LocalNominationResultType.LIST_DRAW));

		// Result Type: Not Elected
		return getElection().getNominations()
				.stream()
				.map(nomination -> new LocalNominationResult(this,
						nomination,
						resultTypes.getOrDefault(nomination, LocalNominationResultType.NOT_ELECTED),
						sainteLague.getOrDefault(nomination, BigDecimal.ZERO)))
				.collect(toCollection(TreeSet::new));
	}

	private Map<LocalNomination, Integer> getVotes() {
		// Calculate
		final Map<LocalNomination, Integer> votes = getBallots().stream()
				.filter(Ballot::isValid)
				.map(LocalBallot::getNominations)
				.flatMap(Set::stream)
				.collect(toMap(Function.identity(), nomination -> 1, (oldValue, thisValue) -> oldValue + thisValue));

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
				.collect(toMap(Function.identity(), party -> 1, (oldValue, thisValue) -> oldValue + thisValue));

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
				.collect(toLinkedHashMap(step -> iterator.next(), Function.identity()));
	}

	private Set<LocalNomination> getSainteLagueNominationsOfParty(final Map<LocalNomination, Integer> votes,
			final Party party) {
		final Set<LocalNomination> nominations = new LinkedHashSet<>();

		// Result Type: Direct and Direct Draw
		getDirectNominations(votes).stream()
				.filter(nomination -> nomination.getParty().map(party::equals).orElse(false))
				.forEach(nominations::add);

		// others
		getElection().getNominations()
				.stream()
				.filter(nomination -> nomination.getParty().map(party::equals).orElse(false))
				.forEach(nominations::add);

		return nominations;
	}

	private BigDecimal getSainteLagueValue(final BigDecimal votesOfParty, final int step) {
		return votesOfParty.divide(BigDecimal.valueOf(10 * step + 5L, 1),
				getElection().getSainteLagueScale(),
				RoundingMode.HALF_UP);
	}

	private Set<LocalNomination> getDirectNominations(final Map<LocalNomination, Integer> votes) {
		return votes.keySet().stream().limit(getElection().getNumberOfDirectSeats()).collect(toLinkedHashSet());
	}

	private Set<LocalNomination> getDrawNominations(final Map<LocalNomination, ? extends Number> votes,
			final Map<LocalNomination, LocalNominationResultType> resultTypes) {
		// Find last nomination
		final Optional<? extends Number> lastNomination
				= votes.values().stream().skip(resultTypes.size() - 1L).findAny();
		if (!lastNomination.isPresent()) {
			return emptySet();
		}

		// Collect probably draw nominations
		final Set<LocalNomination> probablyDirectNominations = votes.entrySet()
				.stream()
				.filter(entry -> entry.getValue().equals(lastNomination.get()))
				.map(Entry::getKey)
				.collect(toCollection(TreeSet::new));
		// TODO: Ergebnis des Losverfahrens

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

	private Set<LocalPartyResult> createPartyResults() {
		return getElection().getParties()
				.stream()
				.map(party -> new LocalPartyResult(this, party))
				.collect(toCollection(TreeSet::new));
	}
}
