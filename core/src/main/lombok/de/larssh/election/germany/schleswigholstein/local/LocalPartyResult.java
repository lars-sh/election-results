package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toLinkedHashSet;
import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.PartyResult;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Wahlergebnis einzelner politischer Parteien und Wählergruppen
 */
@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LocalPartyResult implements PartyResult<LocalBallot>, Comparable<LocalPartyResult> {
	/**
	 * Comparator by election, number of votes (high to low) and party
	 */
	private static final Comparator<LocalPartyResult> COMPARATOR
			= Comparator.<LocalPartyResult, Election<?, ?>>comparing(result -> result.getElectionResult().getElection())
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
			.filter(nominationResult -> nominationResult.getType() != LocalNominationResultType.NOT_ELECTED
					&& nominationResult.getNomination().getParty().filter(getParty()::equals).isPresent())
			.count());

	/**
	 * Anzahl der Stimmen für diese politische Partei oder Wählerguppe
	 */
	Supplier<Integer> numberOfVotes = lazy(() -> (int) getElectionResult().getBallots()
			.stream()
			.filter(Ballot::isValid)
			.map(Ballot::getNominations)
			.flatMap(Collection::stream)
			.filter(nomination -> nomination.getParty().filter(getParty()::equals).isPresent())
			.count());

	/** {@inheritDoc} */
	@Override
	public int compareTo(@Nullable final LocalPartyResult nominationResult) {
		return COMPARATOR.compare(this, nominationResult);
	}

	/** {@inheritDoc} */
	@Override
	public List<LocalBallot> getBallots() {
		return ballots.get();
	}

	public Set<LocalNomination> getDirectNominations() {
		return getNominations().stream()
				.filter(nomination -> nomination.getType() == LocalNominationType.DIRECT)
				.collect(toLinkedHashSet());
	}

	public Set<LocalNomination> getNominations() {
		return getElectionResult().getElection().getNominationsOfParty(getParty());
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
	 * Anzahl der Sitze für diese politische Partei oder Wählerguppe
	 *
	 * @return Anzahl der Sitze
	 */
	public int getNumberOfSeats() {
		return numberOfSeats.get();
	}

	/** {@inheritDoc} */
	@Override
	public int getNumberOfVotes() {
		return numberOfVotes.get();
	}
}
