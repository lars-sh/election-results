package de.larssh.election.germany.schleswigholstein.local;

import java.awt.Color;
import java.util.Set;

import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.DistrictType;
import lombok.NonNull;

public interface LocalElection extends Election {
	
	LocalListNomination createListenvorschlag();

	
	LocalNominationDirect createUnmittelbarenVorschlag();

	
	@Override
	default Color getColorOfStimmzettel() {
		return getGebiet().getType() == DistrictType.KREIS ? Color.RED : Color.WHITE;
	}

	
	@Override
	LocalElectionResult getErgebnis();

	
	Set<LocalListNomination> getListenvorschlaege();

	default int getNumOfListenvertreter() {
		return getNumOfVertreter() - getNumOfUnmittelbareVertreter();
	}

	@Override
	default int getNumOfStimmen() {
		return getNumOfUnmittelbareVertreterPerWahlkreis();
	}

	int getNumOfUnmittelbareVertreter();

	int getNumOfUnmittelbareVertreterPerWahlkreis();

	int getNumOfVertreter();

	int getNumOfWahlkreise();

	
	Set<LocalNominationDirect> getUnmittelbareVorschlaege();

	
	@Override
	Set<LocalNomination> getVorschlaege();
}
