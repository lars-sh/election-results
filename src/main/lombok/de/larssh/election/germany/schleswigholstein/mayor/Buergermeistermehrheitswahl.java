package de.larssh.election.germany.schleswigholstein.mayor;

import java.time.LocalDate;
import java.util.Optional;

import de.larssh.utils.Either;
import lombok.NonNull;

public interface Buergermeistermehrheitswahl extends Buergermeisterwahl {
	
	@Override
	default Either<Buergermeistermehrheitswahl, Buergermeisterstichwahl> either() {
		return Either.ofFirst(this);
	}

	
	@Override
	Buergermeistermehrheitswahlergebnis getErgebnis();

	
	LocalDate getStichwahlDatum();

	
	Optional<Buergermeisterstichwahl> getStichwahl();
}
