package de.larssh.election.germany.schleswigholstein.mayor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalInt;

import de.larssh.utils.Either;

public interface Buergermeistermehrheitswahl extends Buergermeisterwahl {
	@Override
	default Either<Buergermeistermehrheitswahl, Buergermeisterstichwahl> either() {
		return Either.ofFirst(this);
	}

	@Override
	Buergermeistermehrheitswahlergebnis getResult(OptionalInt numberOfBallots);

	LocalDate getStichwahlDatum();

	Optional<Buergermeisterstichwahl> getStichwahl();
}
