package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.utils.Collectors.toLinkedHashSet;
import static de.larssh.utils.Finals.lazy;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
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
	 * @return Art der Vertreterinn oder des Vertreters
	 */
	LocalNominationResultType type;

	/**
	 * Sainte Laguë value
	 *
	 * @return the Sainte Laguë value
	 */
	BigDecimal sainteLagueValue;

	/**
	 * Stimmzettel der Bewerberin oder des Bewerbers
	 */
	@ToString.Exclude
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
		final long numberOfEvaluatedBallotsOfDistrict = getElectionResult().getBallots()
				.stream()
				.filter(ballot -> ballot.getPollingStation().getParent().get().equals(district))
				.count();
		return getNumberOfVotes() > numberOfVotesOfLastDirectNomination
				+ numberOfAllBallotsOfDistrict.getAsInt()
				- numberOfEvaluatedBallotsOfDistrict;
	});

	/**
	 * Determines if the nomination's election is certain and returns the guaranteed
	 * {@link LocalNominationResultType}. In case no result type is certain empty is
	 * returned.
	 *
	 * <p>
	 * This value is not 100% precise in case not all ballots were evaluated, yet.
	 * Even if it returns empty there might be very rare cases of a certainty.
	 */
	@ToString.Exclude
	Supplier<Optional<LocalNominationResultType>> certainResultType = lazy(() -> {
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

	/**
	 * Determines if the nomination is a candidate for
	 * {@link LocalNominationResultType#DIRECT}.
	 */
	Supplier<Boolean> directResultCandidate = lazy(() -> {
		// The number of all ballots is required to calculate the number of direct
		// result candidates.
		final OptionalInt numberOfAllBallots = getElectionResult().getNumberOfAllBallots();
		if (!numberOfAllBallots.isPresent()) {
			return Boolean.FALSE;
		}

		// In case of no votes there's no direct election unless there are remaining
		// unevaluated ballots.
		final int numberfOfEvaluatedBallots = getElectionResult().getBallots().size();
		if (getNumberOfVotes() < 1) {
			return numberfOfEvaluatedBallots < numberOfAllBallots.getAsInt();
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
				- numberfOfEvaluatedBallots >= numberOfVotesOfLastDirectNomination;
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
	 * <p>
	 * This method is not 100% precise in case not all ballots were evaluated, yet.
	 * Even if it returns empty there might be very rare cases of a certainty.
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
	 * <p>
	 * This method is not 100% precise in case not all ballots were evaluated, yet.
	 * Even if it returns {@code false} there might be very rare cases of a certain
	 * election.
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

		// The nomination's party needs at least one certain seat
		final LocalPartyResult partyResult = getElectionResult().getPartyResults().get(party.get());
		final int numberOfCertainSeatsOfParty = partyResult.getNumberOfCertainSeats();
		if (numberOfCertainSeatsOfParty <= 0) {
			return false;
		}

		// In case of certain directly elected nominations of the party the number of
		// possibly certain list seats needs to be reduced.
		final Set<LocalNomination> certainDirectNominationsOfParty = partyResult.getNominationResults()
				.values()
				.stream()
				.filter(LocalNominationResult::isCertainDirectResult)
				.map(LocalNominationResult::getNomination)
				.collect(toLinkedHashSet());
		final int numberOfPossiblyCertainListSeatsOfParty
				= numberOfCertainSeatsOfParty - certainDirectNominationsOfParty.size();
		if (numberOfPossiblyCertainListSeatsOfParty <= 0) {
			return false;
		}

		// The nomination must be part of the nominations of that party without a
		// certain direct seat and in the position of a possibly certain list seat.
		final int indexInNominationsOfPartyWithoutCertainDirectResults = getElection().getNominations(party.get())
				.stream()
				.filter(nomination -> !certainDirectNominationsOfParty.contains(nomination))
				.collect(toList())
				.indexOf(getNomination());
		if (indexInNominationsOfPartyWithoutCertainDirectResults == -1
				|| indexInNominationsOfPartyWithoutCertainDirectResults >= numberOfPossiblyCertainListSeatsOfParty) {
			return false;
		}

		// Because direct result candidates of that party could still take a seat, their
		// number needs to be subtracted from the number of possible certain list seats.
		// The index of the nomination needs to be less than that number.
		final long numberOfDirectResultCandidatesOfParty = getElection().getNominations(party.get())
				.stream()
				.filter(nomination -> !getNomination().equals(nomination)
						&& !certainDirectNominationsOfParty.contains(nomination)
						&& getElectionResult().getNominationResults().get(nomination).directResultCandidate.get())
				.count();
		return indexInNominationsOfPartyWithoutCertainDirectResults < numberOfPossiblyCertainListSeatsOfParty
				- numberOfDirectResultCandidatesOfParty;
	}

	/**
	 * Determines if the nomination's result is a certain
	 * {@link LocalNominationResultType.NOT_ELECTED}.
	 *
	 * <p>
	 * This method is not 100% precise in case not all ballots were evaluated, yet.
	 * Even if it returns {@code false} there might be very rare cases of a certain
	 * election.
	 *
	 * @return {@code true} if the nomination's result is a certain list result,
	 *         else {@code false}
	 */
	private boolean isCertainNotElectedResult() {
		// The number of all ballots is required to calculate the number of not yet
		// evaluated ballots.
		final OptionalInt numberOfAllBallots = getElectionResult().getNumberOfAllBallots();
		if (!numberOfAllBallots.isPresent()) {
			return false;
		}

		// In case all ballots were evaluated we already know the precise result.
		if (getElectionResult().getBallots().size() >= numberOfAllBallots.getAsInt()) {
			return getType() == LocalNominationResultType.NOT_ELECTED;
		}

		return false; // TODO
	}
}
