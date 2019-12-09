package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.NominationResult;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(onParam_ = { @Nullable })
public class LocalNominationResult implements NominationResult<LocalBallot>, Comparable<LocalNominationResult> {
	private static final Comparator<LocalNominationResult> COMPARATOR
			= Comparator.<LocalNominationResult, Election>comparing(result -> result.getElectionResult().getElection())
					.thenComparing(NominationResult::isElected)
					.thenComparing(result -> result.getBallots().size())
					.thenComparing(LocalNominationResult::getNomination);

	LocalElectionResult electionResult;

	LocalNomination nomination;

	List<LocalBallot> ballots;

	LocalNominationResultType type;

	// TODO: BigDecimal sainteLague

	@PackagePrivate
	public LocalNominationResult(final LocalElectionResult electionResult,
			final LocalNomination nomination,
			final List<LocalBallot> ballots,
			final LocalNominationResultType type) {
		this.electionResult = electionResult;
		this.nomination = nomination;
		this.ballots = unmodifiableList(new ArrayList<>(ballots));
		this.type = type;
	}

	@Override
	public int compareTo(@Nullable final LocalNominationResult nominationResult) {
		return COMPARATOR.compare(this, nominationResult);
	}

	@Override
	public boolean isElected() {
		return type != LocalNominationResultType.NOT_ELECTED;
	}
}
