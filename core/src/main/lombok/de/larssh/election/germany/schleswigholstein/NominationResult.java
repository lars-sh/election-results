package de.larssh.election.germany.schleswigholstein;

import java.util.List;

/**
 * Wahlergebnis einzelner Bewerberinnen und Bewerber
 *
 * @param <B> the type of ballots
 * @param <N> the type of nominations
 */
public interface NominationResult<B extends Ballot<? extends N>, N extends Nomination<? extends N>> {
	/**
	 * Wahlergebnis
	 *
	 * @return Wahlergebnis
	 */
	ElectionResult<? extends B, ? extends N> getElectionResult();

	/**
	 * Bewerberin oder Bewerber
	 *
	 * @return Bewerberin oder Bewerber
	 */
	N getNomination();

	/**
	 * Stimmzettel der Bewerberin oder des Bewerbers
	 *
	 * @return Stimmzettel der Bewerberin oder des Bewerbers
	 */
	List<? extends B> getBallots();
}
