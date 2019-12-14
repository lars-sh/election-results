package de.larssh.election.germany.schleswigholstein.local.ui;

import de.larssh.election.germany.schleswigholstein.local.LocalDistrictType;
import de.larssh.utils.Nullables;
import de.larssh.utils.javafx.ChildController;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import lombok.experimental.NonFinal;

public abstract class LocalElectionUiController extends ChildController<MainController> {
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

	public LocalElectionUiController(final MainController parent) {
		super(parent);
	}

	protected TextField getDistrict() {
		return Nullables.orElseThrow(district);
	}

	protected ChoiceBox<LocalDistrictType> getDistrictType() {
		return Nullables.orElseThrow(districtType);
	}

	protected TextField getName() {
		return Nullables.orElseThrow(name);
	}

	protected DatePicker getDate() {
		return Nullables.orElseThrow(date);
	}

	protected Spinner<Integer> getSainteLagueScale() {
		return Nullables.orElseThrow(sainteLagueScale);
	}

	protected CheckBox getPopulationIsPresent() {
		return Nullables.orElseThrow(populationIsPresent);
	}

	protected Spinner<Integer> getPopulation() {
		return Nullables.orElseThrow(population);
	}

	protected CheckBox getNumberOfEligibleVotersIsPresent() {
		return Nullables.orElseThrow(numberOfEligibleVotersIsPresent);
	}

	protected Spinner<Integer> getNumberOfEligibleVoters() {
		return Nullables.orElseThrow(numberOfEligibleVoters);
	}
}
