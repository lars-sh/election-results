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

/**
 * Wahlergebnis einzelner Vertreterinnen und Vertreter
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LocalNominationResult
		implements NominationResult<LocalBallot, LocalNomination>, Comparable<LocalNominationResult> {
	/**
	 * Comparator by election, nomination result type, Sainte Laguë value and
	 * nomination order
	 */
	private static final Comparator<LocalNominationResult> COMPARATOR = Comparator
			.<LocalNominationResult, Election<?, ?>>comparing(result -> result.getElectionResult().getElection())
			.thenComparing(LocalNominationResult::getType)
			.reversed()
			.thenComparing(LocalNominationResult::getSainteLagueValue)
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
	LocalNomination nomination;

	/**
	 * Art der Vertreterin oder des Vertreters gem. § 9+10 GKWG
	 *
	 * @return Art der Vertreterinn oder des Vertreters
	 */
	LocalNominationResultType type;

	BigDecimal sainteLagueValue;

	/**
	 * Stimmzettel der Vertreterin oder des Vertreters
	 */
	Supplier<List<LocalBallot>> ballots = lazy(() -> unmodifiableList(getElectionResult().getBallots()
			.stream()
			.filter(Ballot::isValid)
			.filter(ballot -> ballot.getNominations().contains(getNomination()))
			.collect(toList())));

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
}
