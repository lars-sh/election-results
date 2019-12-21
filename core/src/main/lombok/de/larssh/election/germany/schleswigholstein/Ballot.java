package de.larssh.election.germany.schleswigholstein;

import java.util.Set;

public interface Ballot {
	Election getElection();

	District<?> getPollingStation();

	boolean isPostalVoter();

	boolean isValid();

	Set<? extends Nomination> getNominations();
}
