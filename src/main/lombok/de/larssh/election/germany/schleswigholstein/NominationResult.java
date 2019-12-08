package de.larssh.election.germany.schleswigholstein;

public interface NominationResult {
	Nomination getNomination();

	int getNumberOfVotes();

	boolean isElected();
}
