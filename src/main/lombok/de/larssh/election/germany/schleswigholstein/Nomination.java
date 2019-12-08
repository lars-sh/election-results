package de.larssh.election.germany.schleswigholstein;

public interface Nomination {
	Election getElection();

	District<?> getDistrict();
}
