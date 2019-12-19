package de.larssh.election.germany.schleswigholstein.local.ui;

import de.larssh.utils.Nullables;
import de.larssh.utils.javafx.Controller;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import lombok.experimental.NonFinal;

public class MainUiController extends Controller {
	@FXML
	@NonFinal
	@Nullable
	MenuItem open;

	@FXML
	@NonFinal
	@Nullable
	MenuItem save;

	@FXML
	@NonFinal
	@Nullable
	MenuItem saveAs;

	@FXML
	@NonFinal
	@Nullable
	MenuItem close;

	@FXML
	@NonFinal
	@Nullable
	MenuItem exit;

	@FXML
	@NonFinal
	@Nullable
	MenuItem about;

	@FXML
	@NonFinal
	@Nullable
	Tab election;

	public MainUiController(final MainApplication application, final Stage stage) {
		super(application, stage);
	}

	protected MenuItem getOpen() {
		return Nullables.orElseThrow(open);
	}

	protected MenuItem getSave() {
		return Nullables.orElseThrow(save);
	}

	protected MenuItem getSaveAs() {
		return Nullables.orElseThrow(saveAs);
	}

	protected MenuItem getClose() {
		return Nullables.orElseThrow(close);
	}

	protected MenuItem getExit() {
		return Nullables.orElseThrow(exit);
	}

	protected MenuItem getAbout() {
		return Nullables.orElseThrow(about);
	}

	protected Tab getElection() {
		return Nullables.orElseThrow(election);
	}
}
