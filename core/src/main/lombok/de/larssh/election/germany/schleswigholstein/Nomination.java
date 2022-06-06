package de.larssh.election.germany.schleswigholstein;

import java.util.Optional;

/**
 * Wahlvorschlag (§ 18 GKWG)
 *
 * @param <N> the type of nominations
 */
public interface Nomination<N extends Nomination<? extends N>> {
	/**
	 * Wahl
	 *
	 * @return Wahl
	 */
	Election<?, ? extends N> getElection();

	/**
	 * District
	 *
	 * @return District
	 */
	District<?> getDistrict();

	/**
	 * Politische Partei, Wählergruppe oder empty für unabhängige Bewerberinnen und
	 * Bewerber
	 *
	 * @return Politische Partei, Wählergruppe oder empty für unabhängige
	 *         Bewerberinnen und Bewerber
	 */
	Optional<Party> getParty();

	/**
	 * Bewerberin oder Bewerber
	 *
	 * @return Bewerberin oder Bewerber
	 */
	Person getPerson();
}
