package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.PartyResult;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LocalPartyResult implements PartyResult<LocalBallot>, Comparable<LocalPartyResult> {
	private static final Comparator<LocalPartyResult> COMPARATOR
			= Comparator.<LocalPartyResult, Election<?, ?>>comparing(result -> result.getElectionResult().getElection())
					.thenComparing(LocalPartyResult::getNumberOfVotes)
					.thenComparing(LocalPartyResult::getParty);

	@ToString.Exclude
	LocalElectionResult electionResult;

	Party party;

	Supplier<List<LocalBallot>> ballots = lazy(() -> unmodifiableList(getElectionResult().getBallots()
			.stream()
			.filter(Ballot::isValid)
			.filter(ballot -> ballot.getNominations()
					.stream()
					.map(Nomination::getParty)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.anyMatch(getParty()::equals))
			.collect(toList())));

	Supplier<Integer> numberOfVotes = lazy(() -> (int) getElectionResult().getBallots()
			.stream()
			.filter(Ballot::isValid)
			.map(Ballot::getNominations)
			.flatMap(Collection::stream)
			.map(Nomination::getParty)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(getParty()::equals)
			.count());

	Supplier<Integer> numberOfBlockVotings
			= lazy(() -> (int) getBallots().stream().filter(LocalBallot::isBlockVoting).count());

	@Override
	public int compareTo(@Nullable final LocalPartyResult nominationResult) {
		return COMPARATOR.compare(this, nominationResult);
	}

	@Override
	public List<LocalBallot> getBallots() {
		return ballots.get();
	}

	@Override
	public int getNumberOfVotes() {
		return numberOfVotes.get();
	}

	public int getNumberOfBlockVotings() {
		return numberOfBlockVotings.get();
	}
}
