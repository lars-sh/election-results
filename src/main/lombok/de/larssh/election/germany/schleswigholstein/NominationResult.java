package de.larssh.election.germany.schleswigholstein;

import java.util.List;

public interface NominationResult<B extends Ballot> {
	ElectionResult<B> getElectionResult();

	Nomination getNomination();

	List<B> getBallots();

	boolean isElected();
}
