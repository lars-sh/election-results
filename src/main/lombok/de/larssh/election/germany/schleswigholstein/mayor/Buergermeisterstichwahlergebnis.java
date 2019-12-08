package de.larssh.election.germany.schleswigholstein.mayor;

import de.larssh.election.germany.schleswigholstein.Person;
import lombok.NonNull;

public interface Buergermeisterstichwahlergebnis extends Buergermeisterwahlergebnis {
	
	Person getBuergermeister();
}
