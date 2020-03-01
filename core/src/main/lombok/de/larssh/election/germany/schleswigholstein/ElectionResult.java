package de.larssh.election.germany.schleswigholstein;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Predicate;

public interface ElectionResult<B extends Ballot> {
	Election getElection();

	OptionalInt getNumberOfAllBallots();

	List<B> getBallots();

	Map<? extends Nomination, ? extends NominationResult<B>> getNominationResults();

	Set<? extends PartyResult<B>> getPartyResults();

	ElectionResult<B> filter(Predicate<B> filter);
}
