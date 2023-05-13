package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toLinkedHashMap;
import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Supplier;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.PartyResult;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Wahlergebnis einzelner politischer Parteien und Wählergruppen
 */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LocalPartyResult implements PartyResult<LocalBallot>, Comparable<LocalPartyResult> {
	/**
	 * Comparator by election, number of votes (high to low) and party
	 */
	private static final Comparator<LocalPartyResult> COMPARATOR
			= Comparator.<LocalPartyResult, Election<?, ?>>comparing(LocalPartyResult::getElection)
					.reversed()
					.thenComparing(LocalPartyResult::getNumberOfVotes)
					.reversed()
					.thenComparing(result -> result.getElection().getParties().indexOf(result.getParty()));

	/**
	 * Wahlergebnis
	 *
	 * @return Wahlergebnis
	 */
	@ToString.Exclude
	LocalElectionResult electionResult;

	/**
	 * Politische Partei oder Wählerguppe
	 *
	 * @return Politische Partei oder Wählerguppe
	 */
	@EqualsAndHashCode.Include
	Party party;

	/**
	 * Stimmzettel mit Stimmen für diese politische Partei oder Wählerguppe
	 */
	@ToString.Exclude
	Supplier<List<LocalBallot>> ballots = lazy(() -> unmodifiableList(getElectionResult().getBallots()
			.stream()
			.filter(Ballot::isValid)
			.filter(ballot -> ballot.getNominations()
					.stream()
					.anyMatch(nomination -> nomination.getParty().filter(getParty()::equals).isPresent()))
			.collect(toList())));

	/**
	 * Determines the party's direct nomination results, which are certain.
	 */
	Supplier<Map<LocalNomination, LocalNominationResult>> certainDirectNominationResults
			= lazy(() -> getNominationResults().entrySet()
					.stream()
					.filter(entry -> entry.getValue().isCertainDirectResult())
					.collect(toLinkedHashMap()));

	/**
	 * Anzahl der Blockstimmen für diese politische Partei oder Wählerguppe
	 */
	Supplier<Integer> numberOfBlockVotings
			= lazy(() -> (int) getBallots().stream().filter(LocalBallot::isBlockVoting).count());

	/**
	 * Anzahl der Sitze für diese politische Partei oder Wählerguppe
	 */
	Supplier<Integer> numberOfSeats = lazy(() -> (int) getElectionResult().getNominationResults()
			.values()
			.stream()
			.filter(nominationResult -> nominationResult.getType().isElected()
					&& nominationResult.getNomination().getParty().filter(getParty()::equals).isPresent())
			.count());

	/**
	 * Anzahl der Stimmen für diese politische Partei oder Wählerguppe
	 */
	Supplier<Integer> numberOfVotes = lazy(() -> getNominationResults().values()
			.stream()
			.mapToInt(nominationResult -> nominationResult.getBallots().size())
			.sum());

	/** {@inheritDoc} */
	@Override
	public int compareTo(@Nullable final LocalPartyResult partyResult) {
		return COMPARATOR.compare(this, partyResult);
	}

	/** {@inheritDoc} */
	@Override
	public List<LocalBallot> getBallots() {
		return ballots.get();
	}

	/**
	 * Wahl
	 *
	 * @return Wahl
	 */
	@EqualsAndHashCode.Include
	private LocalElection getElection() {
		return getElectionResult().getElection();
	}

	/**
	 * Bewerberinnen und Bewerber der Gruppierung
	 *
	 * @return Bewerberinnen und Bewerber
	 */
	public Map<LocalNomination, LocalNominationResult> getNominationResults() {
		return getElectionResult().getNominationResults()
				.entrySet()
				.stream()
				.filter(entry -> entry.getKey().getParty().filter(getParty()::equals).isPresent())
				.collect(toLinkedHashMap());
	}

	/**
	 * Anzahl der Blockstimmen für diese politische Partei oder Wählerguppe
	 *
	 * @return Anzahl der Blockstimmen
	 */
	public int getNumberOfBlockVotings() {
		return numberOfBlockVotings.get();
	}

	/**
	 * Determines the number of certain seats of this party.
	 *
	 * @return the number of certain seats of this party
	 */
	public int getNumberOfCertainSeats() {
		// There must be at least one for for this party
		if (getNumberOfVotes() == 0) {
			return 0;
		}

		// There might be less nominations than seats
		if (getElection().getNominations().size() <= getElection().getNumberOfSeats()
				|| getElection().getNominations().stream().map(LocalNomination::getParty).distinct().count() == 1) {
			return Math.min(getElection().getNominations().size(), getNominationResults().size());
		}

		return Math.max(getCertainDirectNominationResults().size(), getNumberOfCertainListSeats());
	}

	/**
	 * Determines the party's direct nomination results, which are certain.
	 *
	 * @return the party's direct nomination results, which are certain
	 */
	@PackagePrivate
	Map<LocalNomination, LocalNominationResult> getCertainDirectNominationResults() {
		return certainDirectNominationResults.get();
	}

	/**
	 * Determines the number of certain list seats of this party, including possibly
	 * certain direct seats.
	 *
	 * @return the number of certain list seats of this party
	 */
	private int getNumberOfCertainListSeats() {
		// The number of all ballots is required to calculate the number of not yet
		// evaluated ballots.
		final OptionalInt numberOfAllBallots = getElectionResult().getNumberOfAllBallots();
		if (!numberOfAllBallots.isPresent()) {
			return 0;
		}

		// In case all ballots were evaluated we already know the precise result.
		if (getElectionResult().getBallots().size() >= numberOfAllBallots.getAsInt()) {
			return getNumberOfSeats();
		}

		// Determine the number of all possible votes to use it as divisor.
		final long numberOfAllPossibleVotes = getElectionResult().getNumberOfVotes()
				+ (numberOfAllBallots.getAsInt() - getElectionResult().getBallots().size())
						* Math.min(getElection().getNumberOfVotesPerBallot(),
								getElection().getDirectNominations(getParty()).size());
		if (numberOfAllPossibleVotes == 0) {
			return 0;
		}

		// Determine the maximum percentage to be sure to get at least one seat ("obere
		// natürliche Sperrklausel") based on the corresponding formula for Sainte Laguë
		// see https://www.wahlrecht.de/verfahren/faktische-sperrklausel.html
		final double maxPercentageForFirstSeat
				= 0.5 / (getElection().getNumberOfSeats() - 0.5 * getElection().getParties().size() + 1);

		// Calculate the number of certain list seats in relation of the number of votes
		// for this party to all possible votes, taking care of the maximum percentage
		// for the first seat.
		final double percentageOfAllBallots = (double) getNumberOfVotes() / numberOfAllPossibleVotes;
		return (int) Math
				.nextDown(getElection().getNumberOfSeats() * (percentageOfAllBallots - maxPercentageForFirstSeat) + 1);
	}

	/**
	 * Calculates the maximum number of nominations, which are a candidate for
	 * {@link LocalNominationResultType#LIST}.
	 *
	 * <p>
	 * Remark: As a small inaccuracy this method does not take care of possible
	 * overhang seats.
	 *
	 * @return the maximum number of list result candidates
	 */
	@PackagePrivate
	int getNumberOfListResultCandidates() {
		// The number of all ballots is required to calculate the number of not yet
		// evaluated ballots.
		final OptionalInt numberOfAllBallots = getElectionResult().getNumberOfAllBallots();
		if (!numberOfAllBallots.isPresent()) {
			return 0;
		}

		// In case all ballots were evaluated we already know the precise result.
		if (getElectionResult().getBallots().size() >= numberOfAllBallots.getAsInt()) {
			return getNumberOfSeats();
		}

		// Determine the maximum number of unevaluated votes to use it as divisor.
		final int numberOfPossiblyMissingVotes
				= (numberOfAllBallots.getAsInt() - getElectionResult().getBallots().size())
						* Math.min(getElection().getNumberOfVotesPerBallot(),
								getElection().getDirectNominations(getParty()).size());
		if (numberOfPossiblyMissingVotes == 0) {
			return getNumberOfSeats();
		}

		// Determine the minimum percentage for the chance to get one seat ("untere
		// natürliche Sperrklausel") based on the corresponding formula for Sainte Laguë
		// see https://www.wahlrecht.de/verfahren/faktische-sperrklausel.html
		final double minPercentageForFirstSeat
				= 0.5 / (getElection().getNumberOfSeats() + 0.5 * getElection().getParties().size() - 1);

		// Calculate the number of list result candidates, taking care of the minimum
		// percentage for the first seat.
		final double percentageOfAllBallots = (double) (getNumberOfVotes() + numberOfPossiblyMissingVotes)
				/ (getElectionResult().getNumberOfVotes() + numberOfPossiblyMissingVotes);
		return (int) Math
				.nextUp(getElection().getNumberOfSeats() * (percentageOfAllBallots - minPercentageForFirstSeat) + 1);
	}

	/**
	 * Anzahl der Sitze für diese politische Partei oder Wählerguppe
	 *
	 * @return Anzahl der Sitze
	 */
	public int getNumberOfSeats() {
		return numberOfSeats.get();
	}

	/** {@inheritDoc} */
	@Override
	@EqualsAndHashCode.Include
	public int getNumberOfVotes() {
		return numberOfVotes.get();
	}
}
