package de.larssh.election.germany.schleswigholstein;

import java.util.List;

public interface NominationResult<B extends Ballot<? extends N>, N extends Nomination<? extends N>> {
	ElectionResult<? extends B, ? extends N> getElectionResult();

	N getNomination();

	List<? extends B> getBallots();
}
