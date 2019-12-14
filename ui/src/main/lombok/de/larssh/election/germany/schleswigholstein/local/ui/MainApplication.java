package de.larssh.election.germany.schleswigholstein.local.ui;

import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {
	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	@SuppressWarnings("unused")
	public void start(@Nullable final Stage primaryStage) {
		new MainController(this, Nullables.orElseThrow(primaryStage));
	}
}
