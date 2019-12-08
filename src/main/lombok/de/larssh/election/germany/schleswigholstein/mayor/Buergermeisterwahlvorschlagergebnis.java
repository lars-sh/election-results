package de.larssh.election.germany.schleswigholstein.mayor;

import de.larssh.election.germany.schleswigholstein.NominationResult;
import lombok.NonNull;

public interface Buergermeisterwahlvorschlagergebnis extends NominationResult {
	
	@Override
	Buergermeisterwahlvorschlag getWahlvorschlag();
}
