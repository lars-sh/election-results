package de.larssh.election.germany.schleswigholstein.mayor;

import java.util.Map;

import de.larssh.election.germany.schleswigholstein.ElectionResult;

public interface Buergermeisterwahlergebnis extends ElectionResult<Buergermeisterwahlstimmzettel> {
	@Override
	Map<Buergermeisterwahlvorschlag, Buergermeisterwahlvorschlagergebnis> getNominationResults();
}
