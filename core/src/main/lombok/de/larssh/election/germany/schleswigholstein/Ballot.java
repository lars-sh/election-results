package de.larssh.election.germany.schleswigholstein;

import java.util.Set;

/**
 * Ballot as a list of selected nominations
 *
 * @param <N> the type of nominations
 */
public interface Ballot<N extends Nomination<? extends N>> {
	/**
	 * Election
	 *
	 * @return the election
	 */
	Election<?, ? extends N> getElection();

	/**
	 * Polling station
	 *
	 * @return the polling station
	 */
	District<?> getPollingStation();

	/**
	 * Predicate specifying, if the ballot was a postal vote
	 *
	 * @return {@code true} if postal vote, else {@code false}
	 */
	boolean isPostalVote();

	/**
	 * Predicate specifying, if the ballot is valid
	 *
	 * @return {@code true} if the vote is valid, else {@code false}
	 */
	boolean isValid();

	/**
	 * Selected nominations
	 *
	 * @return the selected nominations
	 */
	Set<? extends N> getNominations();
}
