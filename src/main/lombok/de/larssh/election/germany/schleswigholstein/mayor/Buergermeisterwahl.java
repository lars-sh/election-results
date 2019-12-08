package de.larssh.election.germany.schleswigholstein.mayor;

import java.util.Set;

import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.utils.Either;
import lombok.NonNull;

public interface Buergermeisterwahl extends Election {
	
	Buergermeisterwahlvorschlag createVorschlag();

	
	Either<Buergermeistermehrheitswahl, Buergermeisterstichwahl> either();

	
	@Override
	Buergermeisterwahlergebnis getErgebnis();

	@Override
	default int getNumOfStimmen() {
		return 1;
	}

	
	@Override
	Set<Buergermeisterwahlvorschlag> getVorschlaege();
}
