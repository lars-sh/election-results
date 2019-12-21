package de.larssh.election.germany.schleswigholstein.local.ui;

import de.larssh.election.germany.schleswigholstein.PartyType;
import de.larssh.utils.Nullables;
import de.larssh.utils.javafx.ChildController;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import lombok.experimental.NonFinal;

public abstract class PartyUiController extends ChildController<LocalElectionController> {
	@FXML
	@NonFinal
	@Nullable
	TextField shortName = null;

	@FXML
	@NonFinal
	@Nullable
	TextField name = null;

	@FXML
	@NonFinal
	@Nullable
	ChoiceBox<PartyType> type = null;

	@FXML
	@NonFinal
	@Nullable
	ColorPicker backgroundColor = null;

	@FXML
	@NonFinal
	@Nullable
	ColorPicker fontColor = null;

	public PartyUiController(final LocalElectionController parent) {
		super(parent);
	}

	protected TextField getShortName() {
		return Nullables.orElseThrow(shortName);
	}

	protected TextField getName() {
		return Nullables.orElseThrow(name);
	}

	protected ChoiceBox<PartyType> getType() {
		return Nullables.orElseThrow(type);
	}

	protected ColorPicker getBackgroundColor() {
		return Nullables.orElseThrow(backgroundColor);
	}

	protected ColorPicker getFontColor() {
		return Nullables.orElseThrow(fontColor);
	}
}
