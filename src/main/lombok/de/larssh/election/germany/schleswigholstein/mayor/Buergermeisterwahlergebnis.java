package de.larssh.election.germany.schleswigholstein.mayor;

import java.util.Set;

import de.larssh.election.germany.schleswigholstein.ElectionResult;

public interface Buergermeisterwahlergebnis extends ElectionResult<Buergermeisterwahlstimmzettel> {
	@Override
	Set<Buergermeisterwahlvorschlagergebnis> getNominationResults();
}
