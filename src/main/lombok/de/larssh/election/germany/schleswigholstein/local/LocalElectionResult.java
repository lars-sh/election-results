package de.larssh.election.germany.schleswigholstein.local;

import java.util.Set;

import de.larssh.election.germany.schleswigholstein.ElectionResult;
import lombok.NonNull;

public interface LocalElectionResult extends ElectionResult {
	
	@Override
	Set<LocalNominationResult> getVorschlagsergebnisse();
}
