package de.larssh.election.germany.schleswigholstein;

import java.util.List;

public interface NominationResult<B extends Ballot> {
	Nomination getNomination();

	List<B> getBallots();

	boolean isElected();
}
