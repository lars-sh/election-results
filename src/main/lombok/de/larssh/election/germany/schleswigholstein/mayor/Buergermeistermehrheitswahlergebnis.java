package de.larssh.election.germany.schleswigholstein.mayor;

import java.util.Optional;

import de.larssh.election.germany.schleswigholstein.Person;
import lombok.NonNull;

public interface Buergermeistermehrheitswahlergebnis extends Buergermeisterwahlergebnis {
	
	Optional<Person> getBuergermeister();
}
