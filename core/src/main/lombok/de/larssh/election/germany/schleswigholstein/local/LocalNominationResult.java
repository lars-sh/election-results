package de.larssh.election.germany.schleswigholstein.local;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import de.larssh.election.germany.schleswigholstein.Ballot;
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
					.thenComparing(LocalNominationResult::getSainteLagueValue)
					.thenComparing(LocalNominationResult::getNomination);

	LocalElectionResult electionResult;

	LocalNomination nomination;

	LocalNominationResultType type;

	BigDecimal sainteLagueValue;

	@PackagePrivate
	public LocalNominationResult(final LocalElectionResult electionResult,
			final LocalNomination nomination,
			final LocalNominationResultType type,
			final BigDecimal sainteLagueValue) {
		this.electionResult = electionResult;
		this.nomination = nomination;
		this.type = type;
		this.sainteLagueValue = sainteLagueValue;
	}

	@Override
	public int compareTo(@Nullable final LocalNominationResult nominationResult) {
		return COMPARATOR.compare(this, nominationResult);
	}

	@Override
	public List<LocalBallot> getBallots() {
		return getElectionResult().getBallots()
				.stream()
				.filter(Ballot::isValid)
				.filter(ballot -> ballot.getNominations().contains(getNomination()))
				.collect(toList());
	}
}
