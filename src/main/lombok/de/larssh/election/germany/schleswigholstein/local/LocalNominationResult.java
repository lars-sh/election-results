package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.larssh.election.germany.schleswigholstein.NominationResult;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LocalNominationResult implements NominationResult<LocalBallot>, Comparable<LocalNominationResult> {
	private static final Comparator<LocalNominationResult> COMPARATOR
			= Comparator.comparing(LocalNominationResult::isElected)
					.thenComparing(result -> result.getBallots().size())
					.thenComparing(LocalNominationResult::getNomination);

	LocalNomination nomination;

	List<LocalBallot> ballots;

	boolean elected;

	// TODO: Mehrsitz, Überhangmandat

	@PackagePrivate
	public LocalNominationResult(final LocalNomination nomination,
			final List<LocalBallot> ballots,
			final boolean elected) {
		this.nomination = nomination;
		this.ballots = unmodifiableList(new ArrayList<>(ballots));
		this.elected = elected;
	}

	@Override
	public int compareTo(@Nullable final LocalNominationResult nominationResult) {
		return COMPARATOR.compare(this, nominationResult);
	}
}
