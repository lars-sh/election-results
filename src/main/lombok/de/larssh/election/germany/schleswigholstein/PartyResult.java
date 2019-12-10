package de.larssh.election.germany.schleswigholstein;

import java.util.List;

public interface PartyResult<B extends Ballot> {
	ElectionResult<B> getElectionResult();

	Party getParty();

	List<B> getBallots();

	int getNumberOfVotes();
}
