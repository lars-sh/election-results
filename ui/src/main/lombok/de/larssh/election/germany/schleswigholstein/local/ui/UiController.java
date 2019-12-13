package de.larssh.election.germany.schleswigholstein.local.ui;

import java.io.IOException;

import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import lombok.Getter;
import lombok.experimental.NonFinal;

@Getter
public class UiController implements Controller {
	Main application;

	LocalElectionController electionController;

	@FXML
	@NonFinal
	@Nullable
	Tab election;

	public UiController(final Main application) {
		super();

		this.application = application;
		electionController = new LocalElectionController(this);
	}

	private Tab getElection() {
		return Nullables.orElseThrow(election);
	}

	@FXML
	private void initialize() throws IOException {
		getElection().setContent(getElectionController().loadFxml());
	}
}
