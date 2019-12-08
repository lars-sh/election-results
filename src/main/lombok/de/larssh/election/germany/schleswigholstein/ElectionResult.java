package de.larssh.election.germany.schleswigholstein;

import java.util.Set;

public interface ElectionResult {
	int getNumberOfBallots();

	int getNumberOfInvalidBallots();

	int getNumberOfValidBallots();

	int getNumberOfPostalVoters();

	Set<? extends NominationResult> getNominationResults();
}
