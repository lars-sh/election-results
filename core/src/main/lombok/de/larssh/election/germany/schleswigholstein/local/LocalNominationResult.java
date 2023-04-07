package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

import de.larssh.election.germany.schleswigholstein.Ballot;
import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.NominationResult;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.utils.annotations.PackagePrivate;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Wahlergebnis einzelner Bewerberinnen und Bewerber
 */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LocalNominationResult
		implements NominationResult<LocalBallot, LocalNomination>, Comparable<LocalNominationResult> {
	/**
	 * Comparator by election, number of votes (high to low) and nomination order
	 */
	private static final Comparator<LocalNominationResult> COMPARATOR
			= Comparator.<LocalNominationResult, Election<?, ?>>comparing(LocalNominationResult::getElection)
					.reversed()
					.thenComparing(LocalNominationResult::getNumberOfVotes)
					.reversed()
					.thenComparing(LocalNominationResult::getNomination);

	/**
	 * Wahlergebnis
	 */
	@ToString.Exclude
	LocalElectionResult electionResult;

	/**
	 * Bewerberin oder Bewerber
	 */
	@EqualsAndHashCode.Include
	LocalNomination nomination;

	/**
	 * Art der Vertreterin oder des Vertreters gem. § 9+10 GKWG
	 *
	 * @return Art der Vertreterin oder des Vertreters
	 */
	LocalNominationResultType type;

	/**
	 * Sainte Laguë value
	 *
	 * <p>
	 * This value is empty in case of a nomination without party.
	 *
	 * @return the Sainte Laguë value or empty
	 */
	Optional<BigDecimal> sainteLagueValue;

	/**
	 * Stimmzettel der Bewerberin oder des Bewerbers
	 */
	@ToString.Exclude
	@Getter(AccessLevel.NONE)
	Supplier<List<LocalBallot>> ballots = lazy(() -> unmodifiableList(getElectionResult().getBallots()
			.stream()
			.filter(Ballot::isValid)
			.filter(ballot -> ballot.getNominations().contains(getNomination()))
			.collect(toList())));

	/**
	 * Determines if the nomination's result is a certain
	 * {@link LocalNominationResultType#DIRECT}.
	 */
	@ToString.Exclude
	@Getter(AccessLevel.NONE)
	Supplier<Boolean> certainDirectResult = lazy(() -> {
		// In case of no votes there's no election
		if (getNumberOfVotes() < 1) {
			return Boolean.FALSE;
		}

		// The number of all ballots of the nomination's district is required to
		// calculate the number of remaining ballots in that district.
		final LocalDistrict district = getNomination().getDistrict();
		final OptionalInt numberOfAllBallotsOfDistrict = getElectionResult().getNumberOfAllBallots(district);
		if (!numberOfAllBallotsOfDistrict.isPresent()) {
			return Boolean.FALSE;
		}

		// The number of votes of the last direct nomination (excluding the current
		// nomination) is used as comparison value.
		final int numberOfVotesOfLastDirectNomination = getElectionResult().getNominationResults()
				.values()
				.stream()
				.filter(nominationResult -> !equals(nominationResult))
				.filter(nominationResult -> nominationResult.getNomination().getDistrict().equals(district))
				.skip(getElection().getNumberOfDirectSeatsPerLocalDistrict() - 1)
				.mapToInt(LocalNominationResult::getNumberOfVotes)
				.findFirst()
				.orElse(0);

		// The number of already evaluated ballots of the nomination's district is
		// required to calculate the number of remaining ballots in that district.
		final int numberOfEvaluatedBallotsOfDistrict = getElectionResult().getBallots(district).size();
		return getNumberOfVotes() > numberOfVotesOfLastDirectNomination
				+ Math.max(numberOfAllBallotsOfDistrict.getAsInt(), numberOfEvaluatedBallotsOfDistrict)
				- numberOfEvaluatedBallotsOfDistrict;
	});

	/**
	 * Determines if the nomination's election is certain and returns the guaranteed
	 * {@link LocalNominationResultType}. In case no result type is certain empty is
	 * returned.
	 */
	@ToString.Exclude
	Supplier<Optional<LocalNominationResultType>> certainResultType = lazy(() -> {
		// The number of all ballots of the nomination's district is required to
		// decide if all ballots were evaluated already.
		final LocalDistrict district = getNomination().getDistrict();
		final OptionalInt numberOfAllBallots = getElectionResult().getNumberOfAllBallots(district);
		if (!numberOfAllBallots.isPresent()) {
			return Optional.empty();
		}

		// If all ballots were evaluated already, the final result can be returned,
		// except for direct draws, which might be overwritten by a certain list result.
		if (getElectionResult().getBallots(district).size() >= numberOfAllBallots.getAsInt()) {
			return Optional.of(getType());
		}

		if (isCertainDirectResult()) {
			return Optional.of(LocalNominationResultType.DIRECT);
		}
		if (isCertainListResult()) {
			return Optional.of(LocalNominationResultType.LIST);
		}
		if (isCertainNotElectedResult()) {
			return Optional.of(LocalNominationResultType.NOT_ELECTED);
		}
		return Optional.empty();
	});

	/** {@inheritDoc} */
	@Override
	public int compareTo(@Nullable final LocalNominationResult nominationResult) {
		return COMPARATOR.compare(this, nominationResult);
	}

	/** {@inheritDoc} */
	@Override
	public List<LocalBallot> getBallots() {
		return ballots.get();
	}

	/**
	 * Determines if the nomination's election is certain and returns the guaranteed
	 * {@link LocalNominationResultType}. In case no result type is certain empty is
	 * returned.
	 *
	 * @return the guaranteed result type or empty
	 */
	public Optional<LocalNominationResultType> getCertainResultType() {
		return certainResultType.get();
	}

	/**
	 * Wahl
	 *
	 * @return Wahl
	 */
	@EqualsAndHashCode.Include
	private LocalElection getElection() {
		return getElectionResult().getElection();
	}

	/** {@inheritDoc} */
	@Override
	@EqualsAndHashCode.Include
	public int getNumberOfVotes() {
		return getBallots().size();
	}

	/**
	 * Determines if the nomination's result is a certain
	 * {@link LocalNominationResultType#DIRECT}.
	 *
	 * @return {@code true} if the nomination's result is a certain direct result,
	 *         else {@code false}
	 */
	@PackagePrivate
	boolean isCertainDirectResult() {
		return certainDirectResult.get();
	}

	/**
	 * Determines if the nomination's result is a certain
	 * {@link LocalNominationResultType#LIST}.
	 *
	 * @return {@code true} if the nomination's result is a certain list result,
	 *         else {@code false}
	 */
	private boolean isCertainListResult() {
		// List results are based on parties.
		final Optional<Party> party = getNomination().getParty();
		if (!party.isPresent()) {
			return false;
		}

		// The nomination's party needs at least one certain seat. In case of certain
		// directly elected nominations of the party the number of possibly certain list
		// seats needs to be reduced.
		final LocalPartyResult partyResult = getElectionResult().getPartyResults().get(party.get());
		final Map<LocalNomination, LocalNominationResult> certainDirectNominationsOfParty
				= partyResult.getCertainDirectNominationResults();
		final int numberOfPossiblyCertainListSeatsOfParty
				= partyResult.getNumberOfCertainSeats() - certainDirectNominationsOfParty.size();
		if (numberOfPossiblyCertainListSeatsOfParty <= 0) {
			return false;
		}

		// The nomination must be part of the nominations of that party without a
		// certain direct seat and in the position of a possibly certain list seat.
		final int indexInNominationsOfPartyWithoutCertainDirectResults = getElection().getListNominations(party.get())
				.stream()
				.filter(nomination -> !certainDirectNominationsOfParty.containsKey(nomination))
				.collect(toList())
				.indexOf(getNomination());
		if (indexInNominationsOfPartyWithoutCertainDirectResults == -1
				|| indexInNominationsOfPartyWithoutCertainDirectResults >= numberOfPossiblyCertainListSeatsOfParty) {
			return false;
		}

		// Because direct result candidates of that party could still take a seat, their
		// number needs to be subtracted from the number of possible certain list seats.
		// The index of the nomination needs to be less than that number.
		final long numberOfDirectResultCandidatesOfParty = getElection().getDirectNominations(party.get())
				.stream()
				.filter(nomination -> !getNomination().equals(nomination)
						&& !certainDirectNominationsOfParty.containsKey(nomination)
						&& getElectionResult().getNominationResults().get(nomination).isDirectResultCandidate())
				.count();
		return indexInNominationsOfPartyWithoutCertainDirectResults < numberOfPossiblyCertainListSeatsOfParty
				- numberOfDirectResultCandidatesOfParty;
	}

	/**
	 * Determines if the nomination's result is a certain
	 * {@link LocalNominationResultType.NOT_ELECTED}.
	 *
	 * @return {@code true} if the nomination's result is a certain list result,
	 *         else {@code false}
	 */
	private boolean isCertainNotElectedResult() {
		return !isDirectResultCandidate() && !isListResultCandidate();
	}

	/**
	 * Determines if the nomination is a candidate for
	 * {@link LocalNominationResultType#DIRECT}.
	 *
	 * @return {@code true} if the nomination is a candidate for a direct result or
	 *         {@code false}
	 */
	private boolean isDirectResultCandidate() {
		// The only chance to be a direct candidate is to have a direct nomination.
		if (!getNomination().isDirectNomination()) {
			return false;
		}

		// The number of all ballots is required to calculate the number of direct
		// result candidates.
		final OptionalInt numberOfAllBallots = getElectionResult().getNumberOfAllBallots();
		if (!numberOfAllBallots.isPresent()) {
			return false;
		}

		// The number of votes plus possibly remaining ballots need to be at least as
		// great as the number of votes of the last direct nomination.
		final int numberOfVotesOfLastDirectNomination = getElectionResult().getNominationResults()
				.values()
				.stream()
				.filter(nominationResult -> nominationResult.getNomination()
						.getDistrict()
						.equals(getNomination().getDistrict()))
				.skip(getElection().getNumberOfDirectSeatsPerLocalDistrict() - 1)
				.mapToInt(LocalNominationResult::getNumberOfVotes)
				.findFirst()
				.orElse(0);
		return getNumberOfVotes()
				+ numberOfAllBallots.getAsInt()
				- getElectionResult().getBallots().size() >= numberOfVotesOfLastDirectNomination;
	}

	/**
	 * Determines if the nomination is a candidate for
	 * {@link LocalNominationResultType#LIST}.
	 *
	 * <p>
	 * Remark: As a quite small inaccuracy this method does not take care of
	 * possible overhang seats.
	 *
	 * @return {@code true} if the nomination is a candidate for a list result or
	 *         {@code false}
	 */
	private boolean isListResultCandidate() {
		// List results are based on parties.
		final Optional<Party> party = getNomination().getParty();
		if (!party.isPresent()) {
			return false;
		}

		// The nomination's party needs at least one list result candidate. In case of
		// certain directly elected nominations of the party the number of list result
		// candidates needs to be reduced.
		final LocalPartyResult partyResult = getElectionResult().getPartyResults().get(party.get());
		final Map<LocalNomination, LocalNominationResult> certainDirectNominationResultsOfParty
				= partyResult.getCertainDirectNominationResults();
		final int numberOfListResultCandidatesOfParty
				= partyResult.getNumberOfListResultCandidates() - certainDirectNominationResultsOfParty.size();
		if (numberOfListResultCandidatesOfParty <= 0) {
			return false;
		}

		// The nomination must be part of the nominations of that party without a
		// certain direct seat and part of the list result candidates.
		final int indexInNominationsOfPartyWithoutCertainDirectResults = getElection().getListNominations(party.get())
				.stream()
				.filter(nomination -> !certainDirectNominationResultsOfParty.containsKey(nomination))
				.collect(toList())
				.indexOf(getNomination());
		return indexInNominationsOfPartyWithoutCertainDirectResults != -1
				&& indexInNominationsOfPartyWithoutCertainDirectResults < numberOfListResultCandidatesOfParty;
	}
}
