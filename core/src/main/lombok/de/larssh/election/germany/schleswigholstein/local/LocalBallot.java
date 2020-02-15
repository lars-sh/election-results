package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.utils.annotations.PackagePrivate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@SuppressWarnings("PMD.DataClass")
public final class LocalBallot implements Ballot {
	@PackagePrivate
	static LocalBallot createInvalidBallot(final LocalElection election,
			final LocalPollingStation pollingStation,
			final boolean postalVoter) {
		return new LocalBallot(election, pollingStation, postalVoter, false, emptySet());
	}

	@PackagePrivate
	static LocalBallot createValidBallot(final LocalElection election,
			final LocalPollingStation pollingStation,
			final boolean postalVoter,
			final Set<LocalNomination> nominations) {
		return new LocalBallot(election, pollingStation, postalVoter, true, nominations);
	}

	@JsonIgnore
	LocalElection election;

	LocalPollingStation pollingStation;

	boolean postalVoter;

	boolean valid;

	Set<LocalNomination> nominations;

	@JsonIgnore
	Supplier<Boolean> blockVoting = lazy(() -> {
		final List<Optional<Party>> parties
				= getNominations().stream().map(LocalNomination::getParty).distinct().collect(toList());
		if (parties.size() != 1) {
			return Boolean.FALSE;
		}

		final Optional<Party> firstParty = parties.get(0);
		if (!firstParty.isPresent()) {
			return Boolean.FALSE;
		}

		final long directNominationsOfThatParty = getElection().getNominationsOfParty(firstParty.get())
				.stream()
				.filter(nomination -> nomination.getType() == LocalNominationType.DIRECT)
				.count();
		return getNominations().size() == directNominationsOfThatParty;
	});

	private LocalBallot(final LocalElection election,
			final LocalPollingStation pollingStation,
			final boolean postalVoter,
			final boolean valid,
			final Set<LocalNomination> nominations) {
		this.election = election;
		this.pollingStation = pollingStation;
		this.valid = valid;
		this.nominations = unmodifiableSet(new TreeSet<>(nominations));
		this.postalVoter = postalVoter;

		for (final LocalNomination nomination : nominations) {
			if (!nomination.getElection().equals(election)) {
				throw new ElectionException(
						"Election \"%s\" of nomination \"%s, %s\" does not match election \"%s\" of ballot.",
						nomination.getElection().getName(),
						nomination.getPerson().getFamilyName(),
						nomination.getPerson().getGivenName(),
						election.getName());
			}
		}
	}

	@JsonIgnore
	public boolean isBlockVoting() {
		return blockVoting.get();
	}
}
