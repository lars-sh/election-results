package de.larssh.election.germany.schleswigholstein.local;

import java.util.Set;

import de.larssh.election.germany.schleswigholstein.Ballot;
import lombok.NonNull;

public interface LocalBallot extends Ballot {
	
	@Override
	Set<LocalNominationDirect> getVorschlaege();

	
	@Override
	LocalElection getWahl();
}
