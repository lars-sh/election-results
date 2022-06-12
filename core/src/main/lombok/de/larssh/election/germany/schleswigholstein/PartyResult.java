package de.larssh.election.germany.schleswigholstein;

import java.util.List;

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

	int getNumberOfVotes();
}
