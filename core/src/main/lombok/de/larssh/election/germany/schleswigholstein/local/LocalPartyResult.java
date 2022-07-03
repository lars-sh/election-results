package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toLinkedHashMap;
import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.PartyResult;
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
					.thenComparing(LocalPartyResult::getParty);

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

	public int getNumberOfCertainSeats() {
		return Math.max(getNumberOfCertainDirectSeats(), getNumberOfCertainListSeats());
	}

	public int getNumberOfCertainDirectSeats() {
		return (int) getNominationResults().values() //
				.stream()
				.filter(LocalNominationResult::isCertainDirectResult)
				.count();
	}

	public int getNumberOfCertainListSeats() {
		final OptionalInt numberOfAllBallots = getElectionResult().getNumberOfAllBallots();
		if (!numberOfAllBallots.isPresent()) {
			return 0;
		}

		final int numberOfAllVotes = getElectionResult().getBallots()
				.stream()
				.filter(LocalBallot::isValid)
				.map(LocalBallot::getNominations)
				.mapToInt(Set::size)
				.sum();
		return (int) Math.floor((double) getNumberOfVotes()
				/ (numberOfAllVotes
						+ (numberOfAllBallots.getAsInt() - getElectionResult().getBallots().size())
								* getElection().getNumberOfVotesPerBallot())
				* (getElection().getNumberOfSeats() + 1 - 0.5 * getElection().getParties().size())
				+ 0.5);
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
