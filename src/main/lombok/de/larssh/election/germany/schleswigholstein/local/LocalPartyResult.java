package de.larssh.election.germany.schleswigholstein.local;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.PartyResult;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(onParam_ = { @Nullable })
public class LocalPartyResult implements PartyResult<LocalBallot>, Comparable<LocalPartyResult> {
	private static final Comparator<LocalPartyResult> COMPARATOR
			= Comparator.<LocalPartyResult, Election>comparing(result -> result.getElectionResult().getElection())
					.thenComparing(LocalPartyResult::getNumberOfVotes)
					.thenComparing(LocalPartyResult::getParty);

	LocalElectionResult electionResult;

	Party party;

	@PackagePrivate
	public LocalPartyResult(final LocalElectionResult electionResult, final Party party) {
		this.electionResult = electionResult;
		this.party = party;
	}

	@Override
	public int compareTo(@Nullable final LocalPartyResult nominationResult) {
		return COMPARATOR.compare(this, nominationResult);
	}

	@Override
	public List<LocalBallot> getBallots() {
		return getElectionResult().getBallots()
				.stream()
				.filter(Ballot::isValid)
				.filter(ballot -> ballot.getNominations()
						.stream()
						.map(Nomination::getParty)
						.filter(Optional::isPresent)
						.map(Optional::get)
						.anyMatch(getParty()::equals))
				.collect(toList());
	}

	@Override
	public int getNumberOfVotes() {
		return (int) getElectionResult().getBallots()
				.stream()
				.filter(Ballot::isValid)
				.map(Ballot::getNominations)
				.flatMap(Collection::stream)
				.map(Nomination::getParty)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.filter(getParty()::equals)
				.count();
	}
}
