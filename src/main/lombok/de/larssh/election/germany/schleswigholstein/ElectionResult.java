package de.larssh.election.germany.schleswigholstein;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface ElectionResult<B extends Ballot> {
	Election getElection();

	List<B> getBallots();

	Set<? extends NominationResult<B>> getNominationResults();

	ElectionResult<B> filter(Predicate<B> filter);
}
