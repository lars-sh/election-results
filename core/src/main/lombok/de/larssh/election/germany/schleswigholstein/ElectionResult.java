package de.larssh.election.germany.schleswigholstein;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Wahlergebnis auf Basis einer ggf. gefilterten Liste an Stimmzetteln
 *
 * @param <B> the type of ballots
 * @param <N> the type of nominations
 */
public interface ElectionResult<B extends Ballot<? extends N>, N extends Nomination<? extends N>> {
	/**
	 * Wahl
	 *
	 * @return Wahl
	 */
	Election<?, ? extends N> getElection();

	/**
	 * Optional number of all ballots of the election
	 *
	 * <p>
	 * In case this number is larger than the size of the list of ballots, then some
	 * ballots were not evaluated, yet.
	 *
	 * @return the number of all ballots of the election or empty
	 */
	@JsonIgnore
	OptionalInt getNumberOfAllBallots();

	/**
	 * Stimmzettel
	 *
	 * @return Stimmzettel
	 */
	List<? extends B> getBallots();

	/**
	 * Wahlergebnis einzelner Bewerberinnen und Bewerber
	 *
	 * @return Wahlergebnis einzelner Bewerberinnen und Bewerber
	 */
	Map<? extends Nomination<? extends N>, ? extends NominationResult<? extends B, ? extends N>> getNominationResults();

	/**
	 * Wahlergebnis einzelner politischer Parteien und Wählergruppen
	 *
	 * @return Wahlergebnis einzelner politischer Parteien und Wählergruppen
	 */
	Map<? extends Party, ? extends PartyResult<? extends B>> getPartyResults();

	/**
	 * Filters the current list of ballots and returns a new {@link ElectionResult}
	 *
	 * @param filter a predicate to test ballots to be part of the new
	 *               {@link ElectionResult}
	 * @return a new {@link ElectionResult} with filtered ballots
	 */
	ElectionResult<? extends B, ? extends N> filter(Predicate<? super B> filter);
}
