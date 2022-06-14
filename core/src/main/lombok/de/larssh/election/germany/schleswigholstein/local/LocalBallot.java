package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.Party;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Stimmzettel
 */
@Getter
@ToString
@EqualsAndHashCode
@SuppressWarnings("PMD.DataClass")
public final class LocalBallot implements Ballot<LocalNomination> {
	/**
	 * Creates an invalid ballot
	 *
	 * @param election       Wahl
	 * @param pollingStation Wahlbezirk
	 * @param postalVote     {@code true} for postal vote ballots, else
	 *                       {@code false}
	 * @return the new ballot
	 */
	public static LocalBallot createInvalidBallot(final LocalElection election,
			final LocalPollingStation pollingStation,
			final boolean postalVote) {
		return new LocalBallot(election, pollingStation, postalVote, false, emptySet());
	}

	/**
	 * Creates a valid ballot
	 *
	 * @param election       Wahl
	 * @param pollingStation Wahlbezirk
	 * @param postalVote     {@code true} for postal vote ballots, else
	 *                       {@code false}
	 * @param nominations    gewählte Bewerberinnen und Bewerber
	 * @return the new ballot
	 */
	public static LocalBallot createValidBallot(final LocalElection election,
			final LocalPollingStation pollingStation,
			final boolean postalVote,
			final Set<LocalNomination> nominations) {
		return new LocalBallot(election, pollingStation, postalVote, true, nominations);
	}

	/**
	 * Wahl
	 *
	 * @return Wahl
	 */
	@JsonIgnore
	LocalElection election;

	/**
	 * Wahlbezirk
	 *
	 * @return Wahlbezirk
	 */
	@JsonIgnore
	LocalPollingStation pollingStation;

	/**
	 * Briefwahl (§ 33 GKWG)
	 *
	 * @return {@code true} for postal vote ballots, else {@code false}
	 */
	boolean postalVote;

	/**
	 * Ungültige Stimme (§ 35 GKWG)
	 *
	 * @return {@code true} for valid ballots, else {@code false}
	 */
	boolean valid;

	/**
	 * Gewählte Bewerberinnen und Bewerber (§ 28 Absatz 2 GKWG)
	 *
	 * @return Gewählte Bewerberinnen und Bewerber
	 */
	Set<LocalNomination> nominations;

	/**
	 * Blockwahl
	 */
	@JsonIgnore
	@ToString.Exclude
	Supplier<Boolean> blockVoting = lazy(() -> {
		final List<Party> parties = getNominations().stream()
				.map(LocalNomination::getParty)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.distinct()
				.limit(2)
				.collect(toList());
		if (parties.size() != 1) {
			return Boolean.FALSE;
		}

		final long directNominationsOfThatParty = getElection().getNominationsOfParty(parties.get(0))
				.stream()
				.filter(nomination -> nomination.getType() == LocalNominationType.DIRECT)
				.count();
		return getNominations().size() == directNominationsOfThatParty;
	});

	/**
	 * Stimmzettel
	 *
	 * @param election       Wahl
	 * @param pollingStation Wahlbezirk
	 * @param postalVote     {@code true} for postal vote ballots, else
	 *                       {@code false}
	 * @param valid          {@code true} for valid ballots, else {@code false}
	 * @param nominations    gewählte Bewerberinnen und Bewerber
	 */
	private LocalBallot(final LocalElection election,
			final LocalPollingStation pollingStation,
			final boolean postalVote,
			final boolean valid,
			final Set<LocalNomination> nominations) {
		this.election = election;
		this.pollingStation = pollingStation;
		this.valid = valid;
		this.nominations = unmodifiableSet(new TreeSet<>(nominations));
		this.postalVote = postalVote;

		if (valid) {
			if (nominations.size() > election.getNumberOfVotesPerBallot()) {
				throw new ElectionException(String.format(
						"Ballot of election \"%s\" contains more (%d) nominations than permitted (%d). Nominations are: %s",
						election.getName(),
						nominations.size(),
						election.getNumberOfVotesPerBallot(),
						this.nominations.stream()
								.map(nomination -> nomination.getPerson().getGivenName()
										+ ' '
										+ nomination.getPerson().getFamilyName())
								.collect(joining(", "))));
			}
			for (final LocalNomination nomination : this.nominations) {
				if (!nomination.getElection().equals(election)) {
					throw new ElectionException(
							"Election \"%s\" of nomination \"%s, %s\" does not match election \"%s\" of ballot.",
							nomination.getElection().getName(),
							nomination.getPerson().getFamilyName(),
							nomination.getPerson().getGivenName(),
							election.getName());
				}
				if (nomination.getType() != LocalNominationType.DIRECT) {
					throw new ElectionException(
							"Nomination \"%s, %s\" of election \"%s\" is not a direct nomination. Ballots can contain direct nominations only.",
							nomination.getPerson().getFamilyName(),
							nomination.getPerson().getGivenName(),
							election.getName());
				}
			}
		}
	}

	/**
	 * Polling station as JSON property
	 *
	 * @return Wahlbezirk
	 */
	@JsonProperty("pollingStation")
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "JSON property")
	private String getPollingStationForJackson() {
		return getPollingStation().getKey();
	}

	/**
	 * Blockwahl
	 *
	 * @return {@code true} if the ballot contains all and only nominations of
	 *         exactly one party, else {@code false}
	 */
	@JsonIgnore
	public boolean isBlockVoting() {
		return blockVoting.get();
	}
}
