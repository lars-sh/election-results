package de.larssh.election.germany.schleswigholstein;

import java.util.Set;

public interface Ballot<N extends Nomination<? extends N>> {
	Election<?, ? extends N> getElection();

	District<?> getPollingStation();

	boolean isPostalVoter();

	boolean isValid();

	Set<? extends N> getNominations();
}
