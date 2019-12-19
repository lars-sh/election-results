package de.larssh.election.germany.schleswigholstein.local.ui;

import java.time.LocalDate;
import java.util.OptionalInt;

import de.larssh.election.germany.schleswigholstein.local.LocalDistrictRoot;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrictType;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.utils.javafx.JavaFxUtils;
import javafx.collections.FXCollections;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;

public class LocalElectionController extends LocalElectionUiController {
	public LocalElectionController(final MainController parent) {
		super(parent);
	}

	public LocalElection getElection() {
		// District
		final LocalDistrictRoot district = new LocalDistrictRoot(getDistrict().getText(), getDistrictType().getValue());

		// Election
		final LocalElection election = new LocalElection(district,
				getDate().getValue(),
				getName().getText(),
				getSainteLagueScale().getValue());

		// Population
		if (getPopulationIsPresent().isSelected()) {
			election.setPopulation(district, getPopulation().getValue());
		}

		// Number of Eligible Voters
		if (getNumberOfEligibleVotersIsPresent().isSelected()) {
			election.setNumberOfEligibleVoters(district, getNumberOfEligibleVoters().getValue());
		}
		return election;
	}

	@Override
	protected void initialize() {
		// District
		getDistrictType().setItems(FXCollections.observableArrayList(LocalDistrictType.values()));

		// Election
		JavaFxUtils.initializeEditableSpinner(getSainteLagueScale(),
				new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));

		// Population
		getPopulationIsPresent().selectedProperty()
				.addListener((observable, oldValue, newValue) -> getPopulation().setDisable(!newValue));
		JavaFxUtils.initializeEditableSpinner(getPopulation(), new IntegerSpinnerValueFactory(71, Integer.MAX_VALUE));

		// Number of Eligible Voters
		getNumberOfEligibleVotersIsPresent().selectedProperty()
				.addListener((observable, oldValue, newValue) -> getNumberOfEligibleVoters().setDisable(!newValue));
		JavaFxUtils.initializeEditableSpinner(getNumberOfEligibleVoters(),
				new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE));

		reset();
	}

	public void reset() {
		// District
		getDistrict().setText("");
		getDistrictType().setValue(LocalDistrictType.KREISANGEHOERIGE_GEMEINDE);

		// Election
		getName().setText("");
		getDate().setValue(LocalDate.now());
		getSainteLagueScale().getValueFactory().setValue(2);

		// Population
		getPopulationIsPresent().setSelected(true);
		getPopulation().getValueFactory().setValue(71);
		getPopulation().setDisable(false);

		// Number of Eligible Voters
		getNumberOfEligibleVotersIsPresent().setSelected(true);
		getNumberOfEligibleVoters().getValueFactory().setValue(1);
		getNumberOfEligibleVoters().setDisable(false);
	}

	public void setElection(final LocalElection election) {
		// District
		getDistrict().setText(election.getDistrict().getName());
		getDistrictType().setValue(election.getDistrict().getType());

		// Election
		getDate().setValue(election.getDate());
		getName().setText(election.getName());
		getSainteLagueScale().getValueFactory().setValue(election.getSainteLagueScale());

		// Population
		final OptionalInt population = election.getPopulation(election.getDistrict());
		getPopulation().getValueFactory().setValue(population.orElse(71));
		getPopulationIsPresent().setSelected(population.isPresent());

		// Number of Eligible Voters
		final OptionalInt numberOfEligibleVoters = election.getNumberOfEligibleVoters(election.getDistrict());
		getNumberOfEligibleVoters().getValueFactory().setValue(numberOfEligibleVoters.orElse(1));
		getNumberOfEligibleVotersIsPresent().setSelected(numberOfEligibleVoters.isPresent());
	}
}
