package de.larssh.election.germany.schleswigholstein.mayor;

import java.util.Set;

import de.larssh.election.germany.schleswigholstein.ElectionResult;
import lombok.NonNull;

public interface Buergermeisterwahlergebnis extends ElectionResult {
	
	@Override
	Set<Buergermeisterwahlvorschlagergebnis> getVorschlagsergebnisse();
}
