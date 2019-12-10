package de.larssh.election.germany.schleswigholstein.ui;

import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lombok.NoArgsConstructor;
import lombok.experimental.NonFinal;

@NoArgsConstructor
public class UiController {
	@FXML
	@NonFinal
	@Nullable
	public Button button = null;

	private Button getButton() {
		return Nullables.orElseThrow(button);
	}

	@FXML
	public void initialize() {
		System.out.println("init");
		getButton().setMinWidth(200);
	}

	@FXML
	public void onButton(@SuppressWarnings("unused") final ActionEvent event) {
		System.out.println("Button pressed.");
	}
}
