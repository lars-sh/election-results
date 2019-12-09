package de.larssh.election.germany.schleswigholstein.mayor;

import de.larssh.election.germany.schleswigholstein.NominationResult;

public interface Buergermeisterwahlvorschlagergebnis extends NominationResult<Buergermeisterwahlstimmzettel> {
	@Override
	Buergermeisterwahlvorschlag getNomination();
}
