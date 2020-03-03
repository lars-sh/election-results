package de.larssh.election.germany.schleswigholstein.mayor;

import de.larssh.election.germany.schleswigholstein.NominationResult;

public interface Buergermeisterwahlvorschlagergebnis
		extends NominationResult<Buergermeisterwahlstimmzettel, Buergermeisterwahlvorschlag> {
	@Override
	Buergermeisterwahlvorschlag getNomination();
}
