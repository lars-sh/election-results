package de.larssh.election.germany.schleswigholstein;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Predicate;

public interface ElectionResult<B extends Ballot<? extends N>, N extends Nomination<? extends N>> {
	Election<?, ? extends N> getElection();

	OptionalInt getNumberOfAllBallots();

	List<? extends B> getBallots();

	Map<? extends Nomination<? extends N>, ? extends NominationResult<? extends B, ? extends N>> getNominationResults();

	Map<? extends Party, ? extends PartyResult<? extends B>> getPartyResults();

	ElectionResult<? extends B, ? extends N> filter(Predicate<? super B> filter);
}
