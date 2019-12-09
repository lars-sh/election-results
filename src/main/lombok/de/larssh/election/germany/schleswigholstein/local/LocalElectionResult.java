package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.ElectionResult;
import de.larssh.utils.annotations.PackagePrivate;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LocalElectionResult implements ElectionResult<LocalBallot> {
	private static Set<LocalNominationResult> calculateNominationResults(final Election election,
			final List<LocalBallot> ballots) {
		return new TreeSet<>(); // TODO: calculateNominationResults
	}

	Election election;

	List<LocalBallot> ballots;

	Set<LocalNominationResult> nominationResults;

	@PackagePrivate
	LocalElectionResult(final Election election, final List<LocalBallot> ballots) {
		this(election, ballots, isEqual(true));
	}

	private LocalElectionResult(final Election election,
			final List<LocalBallot> ballots,
			final Predicate<LocalBallot> filter) {
		this.election = election;
		this.ballots = unmodifiableList(ballots.stream().filter(filter).collect(toList()));
		nominationResults = unmodifiableSet(calculateNominationResults(election, this.ballots));
	}

	@Override
	public ElectionResult<LocalBallot> filter(final Predicate<LocalBallot> filter) {
		return new LocalElectionResult(getElection(), getBallots(), filter);
	}
}
