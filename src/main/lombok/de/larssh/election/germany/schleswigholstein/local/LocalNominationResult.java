package de.larssh.election.germany.schleswigholstein.local;

import de.larssh.election.germany.schleswigholstein.NominationResult;
import lombok.NonNull;

public interface LocalNominationResult extends NominationResult {
	
	@Override
	LocalNomination getWahlvorschlag();

	// TODO: Mehrsitz, Überhangmandat
}
