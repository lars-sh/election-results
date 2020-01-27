package de.larssh.election.germany.schleswigholstein.local.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.OptionalInt;

import de.larssh.election.germany.schleswigholstein.local.LocalDistrictRoot;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrictType;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.utils.javafx.JavaFxUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.collections.FXCollections;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import lombok.Getter;

@Getter
public class LocalElectionController extends LocalElectionUiController {
	private static final int POPULATION_DEFAULT = 71;

	PartyController partyController;

	LocalDistrictController localDistrictController;

	public LocalElectionController(final MainController parent) {
		super(parent);

		partyController = new PartyController(this);
		localDistrictController = new LocalDistrictController(this);
	}

	public LocalElection getElection() {
		// Election
		final LocalDistrictRoot district = new LocalDistrictRoot(getDistrict().getText(), getDistrictType().getValue());
		final LocalElection election = new LocalElection(district,
				getDate().getValue(),
				getName().getText(),
				getSainteLagueScale().getValue());
		if (getPopulationIsPresent().isSelected()) {
			election.setPopulation(district, getPopulation().getValue());
		}
		if (getNumberOfEligibleVotersIsPresent().isSelected()) {
			election.setNumberOfEligibleVoters(district, getNumberOfEligibleVoters().getValue());
		}

		// Districts
		// TODO

		return election;
	}

	@Override
	@SuppressFBWarnings(value = "EXS_EXCEPTION_SOFTENING_NO_CHECKED",
			justification = "missing FXML files are true runtime expections")
	protected void initialize() {
		// Election
		getDistrictType().setItems(FXCollections.observableArrayList(LocalDistrictType.values()));
		JavaFxUtils.initializeEditableSpinner(getSainteLagueScale(),
				new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
		getPopulationIsPresent().selectedProperty()
				.addListener((observable, oldValue, newValue) -> getPopulation().setDisable(!newValue));
		JavaFxUtils.initializeEditableSpinner(getPopulation(),
				new IntegerSpinnerValueFactory(POPULATION_DEFAULT, Integer.MAX_VALUE));
		getNumberOfEligibleVotersIsPresent().selectedProperty()
				.addListener((observable, oldValue, newValue) -> getNumberOfEligibleVoters().setDisable(!newValue));
		JavaFxUtils.initializeEditableSpinner(getNumberOfEligibleVoters(),
				new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE));

		// Parties
		try {
			getParty().getChildren().add(getPartyController().loadFxml());
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}

		// Districts
		try {
			getLocalDistrict().getChildren().add(getLocalDistrictController().loadFxml());
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}

		reset();
	}

	public void reset() {
		// Election
		// TODO: select first tab
		getDistrict().setText("");
		getDistrictType().setValue(LocalDistrictType.KREISANGEHOERIGE_GEMEINDE);
		getName().setText("");
		getDate().setValue(LocalDate.now());
		getSainteLagueScale().getValueFactory().setValue(2);
		getPopulationIsPresent().setSelected(true);
		getPopulation().getValueFactory().setValue(POPULATION_DEFAULT);
		getPopulation().setDisable(false);
		getNumberOfEligibleVotersIsPresent().setSelected(true);
		getNumberOfEligibleVoters().getValueFactory().setValue(1);
		getNumberOfEligibleVoters().setDisable(false);

		// Parties
		getParties().setItems(FXCollections.observableArrayList(PartyChoiceEntry.empty()));
		getParties().setValue(PartyChoiceEntry.empty());
		getPartyController().reset();
		getLocalDistrictController().reset();

		// Districts
		// TODO
	}

	public void setElection(final LocalElection election) {
		// Election
		getDistrict().setText(election.getDistrict().getName());
		getDistrictType().setValue(election.getDistrict().getType());
		getDate().setValue(election.getDate());
		getName().setText(election.getName());
		getSainteLagueScale().getValueFactory().setValue(election.getSainteLagueScale());
		final OptionalInt population = election.getPopulation(election.getDistrict());
		getPopulation().getValueFactory().setValue(population.orElse(POPULATION_DEFAULT));
		getPopulationIsPresent().setSelected(population.isPresent());
		final OptionalInt numberOfEligibleVoters = election.getNumberOfEligibleVoters(election.getDistrict());
		getNumberOfEligibleVoters().getValueFactory().setValue(numberOfEligibleVoters.orElse(1));
		getNumberOfEligibleVotersIsPresent().setSelected(numberOfEligibleVoters.isPresent());

		// Parties
		// TODO

		// Districts
		// TODO
	}
}
