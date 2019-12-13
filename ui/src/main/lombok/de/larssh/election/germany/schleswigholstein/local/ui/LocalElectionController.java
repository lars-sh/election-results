package de.larssh.election.germany.schleswigholstein.local.ui;

import java.time.LocalDate;
import java.util.OptionalInt;

import de.larssh.election.germany.schleswigholstein.local.LocalDistrictSuper;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrictType;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.experimental.NonFinal;

@Getter
public class LocalElectionController implements Controller {
	UiController uiController;

	@FXML
	@NonFinal
	@Nullable
	TextField district = null;

	@FXML
	@NonFinal
	@Nullable
	ChoiceBox<LocalDistrictType> districtType = null;

	@FXML
	@NonFinal
	@Nullable
	TextField name = null;

	@FXML
	@NonFinal
	@Nullable
	DatePicker date = null;

	@FXML
	@NonFinal
	@Nullable
	Spinner<Integer> sainteLagueScale = null;

	@FXML
	@NonFinal
	@Nullable
	CheckBox populationIsPresent = null;

	@FXML
	@NonFinal
	@Nullable
	Spinner<Integer> population = null;

	@FXML
	@NonFinal
	@Nullable
	CheckBox numberOfEligibleVotersIsPresent = null;

	@FXML
	@NonFinal
	@Nullable
	Spinner<Integer> numberOfEligibleVoters = null;

	public LocalElectionController(final UiController uiController) {
		this.uiController = uiController;
	}

	private TextField getDistrict() {
		return Nullables.orElseThrow(district);
	}

	private ChoiceBox<LocalDistrictType> getDistrictType() {
		return Nullables.orElseThrow(districtType);
	}

	private TextField getName() {
		return Nullables.orElseThrow(name);
	}

	private DatePicker getDate() {
		return Nullables.orElseThrow(date);
	}

	private Spinner<Integer> getSainteLagueScale() {
		return Nullables.orElseThrow(sainteLagueScale);
	}

	private CheckBox getPopulationIsPresent() {
		return Nullables.orElseThrow(populationIsPresent);
	}

	private Spinner<Integer> getPopulation() {
		return Nullables.orElseThrow(population);
	}

	private CheckBox getNumberOfEligibleVotersIsPresent() {
		return Nullables.orElseThrow(numberOfEligibleVotersIsPresent);
	}

	private Spinner<Integer> getNumberOfEligibleVoters() {
		return Nullables.orElseThrow(numberOfEligibleVoters);
	}

	@FXML
	private void initialize() {
		// District
		getDistrictType().setItems(FXCollections.observableArrayList(LocalDistrictType.values()));

		// Election
		getSainteLagueScale().setValueFactory(new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));

		// Population
		getPopulationIsPresent().selectedProperty()
				.addListener((observable, oldValue, newValue) -> getPopulation().setDisable(!newValue));

		getPopulation().setValueFactory(new IntegerSpinnerValueFactory(71, Integer.MAX_VALUE));

		// Number of Eligible Voters
		getNumberOfEligibleVoters().setValueFactory(new IntegerSpinnerValueFactory(1, Integer.MAX_VALUE));
		getNumberOfEligibleVotersIsPresent().selectedProperty()
				.addListener((observable, oldValue, newValue) -> getNumberOfEligibleVoters().setDisable(!newValue));

		reset();
	}

	public LocalElection getElection() {
		// District
		final LocalDistrictSuper district
				= new LocalDistrictSuper(getDistrict().getText(), getDistrictType().getValue());

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
			election.setPopulation(district, getNumberOfEligibleVoters().getValue());
		}
		return election;
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
		getPopulationIsPresent().setSelected(true); // TODO
		getPopulation().getValueFactory().setValue(71);
		getPopulation().setDisable(false);

		// Number of Eligible Voters
		getNumberOfEligibleVotersIsPresent().setSelected(true); // TODO
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
		getPopulationIsPresent().setSelected(population.isPresent()); // TODO

		// Number of Eligible Voters
		final OptionalInt numberOfEligibleVoters = election.getNumberOfEligibleVoters(election.getDistrict());
		getNumberOfEligibleVoters().getValueFactory().setValue(numberOfEligibleVoters.orElse(1));
		getNumberOfEligibleVotersIsPresent().setSelected(numberOfEligibleVoters.isPresent()); // TODO
	}
}
