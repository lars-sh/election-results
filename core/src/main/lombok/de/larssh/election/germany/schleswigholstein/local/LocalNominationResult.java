package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.NominationResult;
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
public class LocalNominationResult
		implements NominationResult<LocalBallot, LocalNomination>, Comparable<LocalNominationResult> {
	private static final Comparator<LocalNominationResult> COMPARATOR = Comparator
			.<LocalNominationResult, Election<?, ?>>comparing(result -> result.getElectionResult().getElection())
			.thenComparing(LocalNominationResult::getSainteLagueValue)
			.thenComparing(LocalNominationResult::getNomination);

	@ToString.Exclude
	LocalElectionResult electionResult;

	LocalNomination nomination;

	LocalNominationResultType type;

	BigDecimal sainteLagueValue;

	Supplier<List<LocalBallot>> ballots = lazy(() -> unmodifiableList(getElectionResult().getBallots()
			.stream()
			.filter(Ballot::isValid)
			.filter(ballot -> ballot.getNominations().contains(getNomination()))
			.collect(toList())));

	@Override
	public int compareTo(@Nullable final LocalNominationResult nominationResult) {
		return COMPARATOR.compare(this, nominationResult);
	}

	@Override
	public List<LocalBallot> getBallots() {
		return ballots.get();
	}
}
