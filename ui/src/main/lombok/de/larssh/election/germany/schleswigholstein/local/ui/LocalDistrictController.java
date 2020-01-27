package de.larssh.election.germany.schleswigholstein.local.ui;

import de.larssh.election.germany.schleswigholstein.local.LocalDistrict;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrictRoot;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;

public class LocalDistrictController extends LocalDistrictUiController {
	public LocalDistrictController(final LocalElectionController parent) {
		super(parent);
	}

	public LocalDistrict createLocalDistrict(final LocalDistrictRoot parent) {
		return parent.createChild(getName().getText());
	}

	public LocalDistrict create(final LocalElection election) {
		// TODO: pollingstations
		// TODO: nominations
		return election.getDistrict().createChild(getName().getText());
	}

	@Override
	protected void initialize() {
		reset();
	}

	public void reset() {
		getName().setText("");
		// TODO: pollingstations
		// TODO: nominations
	}

	public void setLocalDistrict(final LocalElection election, final LocalDistrict localDistrict) {
		getName().setText(localDistrict.getName());
		// TODO: pollingstations
		// TODO: nominations
	}
}
