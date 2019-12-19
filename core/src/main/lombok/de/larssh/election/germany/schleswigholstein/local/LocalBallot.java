package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Collections.unmodifiableSet;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(onParam_ = { @Nullable })
public class LocalBallot implements Ballot {
	@JsonIgnore
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

	@JsonIgnore
	public boolean isBlockwahl() {
		final Optional<Party> anyParty = getNominations().stream() //
				.findAny()
				.flatMap(Nomination::getParty);
		if (!anyParty.isPresent()) {
			return false;
		}

		final boolean allOfThatParty = getNominations().stream() //
				.map(Nomination::getParty)
				.allMatch(anyParty::equals);
		if (!allOfThatParty) {
			return false;
		}

		final long directNominationsOfThatParty = getElection().getNominations()
				.stream()
				.filter(nomination -> nomination.getParty().equals(anyParty))
				.filter(nomination -> nomination.getType() == LocalNominationType.DIRECT)
				.count();
		return getNominations().size() == directNominationsOfThatParty;
	}
}
