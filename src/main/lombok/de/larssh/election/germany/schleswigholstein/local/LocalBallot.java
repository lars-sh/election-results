package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Collections.unmodifiableSet;

import java.util.Set;
import java.util.TreeSet;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(onParam_ = { @Nullable })
public class LocalBallot implements Ballot {
	LocalElection election;

	LocalPollingStation pollingStation;

	boolean valid;

	Set<LocalNomination> nominations;

	boolean postalVoter;

	@PackagePrivate
	LocalBallot(final LocalElection election,
			final LocalPollingStation pollingStation,
			final boolean valid,
			final Set<LocalNomination> nominations,
			final boolean postalVoter) {
		this.election = election;
		this.pollingStation = pollingStation;
		this.valid = valid;
		this.nominations = unmodifiableSet(new TreeSet<>(nominations));
		this.postalVoter = postalVoter;
	}
}
