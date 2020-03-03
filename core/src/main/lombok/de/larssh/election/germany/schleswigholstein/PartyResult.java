package de.larssh.election.germany.schleswigholstein;

import java.util.List;

public interface PartyResult<B extends Ballot<?>> {
	ElectionResult<? extends B, ?> getElectionResult();

	Party getParty();

	List<? extends B> getBallots();

	int getNumberOfVotes();
}
