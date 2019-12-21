package de.larssh.election.germany.schleswigholstein.local.ui;

import java.util.Optional;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrict;
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
import javafx.scene.layout.Pane;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

	@FXML
	@NonFinal
	@Nullable
	ChoiceBox<LocalDistrictChoiceEntry> localDistricts = null;

	@FXML
	@NonFinal
	@Nullable
	Pane localDistrict = null;

	@FXML
	@NonFinal
	@Nullable
	ChoiceBox<PartyChoiceEntry> parties = null;

	@FXML
	@NonFinal
	@Nullable
	Pane party = null;

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

	protected ChoiceBox<PartyChoiceEntry> getParties() {
		return Nullables.orElseThrow(parties);
	}

	protected Pane getParty() {
		return Nullables.orElseThrow(party);
	}

	protected ChoiceBox<LocalDistrictChoiceEntry> getLocalDistricts() {
		return Nullables.orElseThrow(localDistricts);
	}

	protected Pane getLocalDistrict() {
		return Nullables.orElseThrow(localDistrict);
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@EqualsAndHashCode(onParam_ = { @Nullable })
	public static class PartyChoiceEntry {
		private static final PartyChoiceEntry EMPTY = new PartyChoiceEntry(Optional.empty());

		public static PartyChoiceEntry empty() {
			return EMPTY;
		}

		public static PartyChoiceEntry of(final Party party) {
			return new PartyChoiceEntry(Optional.of(party));
		}

		Optional<Party> party;

		@Override
		public String toString() {
			return getParty().map(party -> String.format("%s (%s)", party.getName(), party.getShortName()))
					.orElse("<neue Gruppierung>");
		}
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@EqualsAndHashCode(onParam_ = { @Nullable })
	public static class LocalDistrictChoiceEntry {
		private static final LocalDistrictChoiceEntry EMPTY = new LocalDistrictChoiceEntry(Optional.empty());

		public static LocalDistrictChoiceEntry empty() {
			return EMPTY;
		}

		public static LocalDistrictChoiceEntry of(final LocalDistrict district) {
			return new LocalDistrictChoiceEntry(Optional.of(district));
		}

		Optional<LocalDistrict> district;

		@Override
		public String toString() {
			return getDistrict().map(District::getName).orElse("<neuer Wahlkreis>");
		}
	}
}
