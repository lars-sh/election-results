package de.larssh.election.germany.schleswigholstein;

import java.util.List;

/**
 * Wahlergebnis einzelner politischer Parteien oder Wählergruppen
 *
 * @param <B> the type of ballots
 */
public interface PartyResult<B extends Ballot<?>> {
	/**
	 * Wahlergebnis
	 *
	 * @return Wahlergebnis
	 */
	ElectionResult<? extends B, ?> getElectionResult();

	/**
	 * Politische Partei oder Wählergruppe
	 *
	 * @return Politische Partei oder Wählergruppe
	 */
	Party getParty();

	/**
	 * Stimmzettel der politischen Partei oder Wählergruppe
	 *
	 * @return Stimmzettel der politischen Partei oder Wählergruppe
	 */
	List<? extends B> getBallots();

	/**
	 * Number of votes for this party
	 *
	 * @return the number of votes for this party
	 */
	int getNumberOfVotes();
}
