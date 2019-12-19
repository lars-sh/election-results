package de.larssh.election.germany.schleswigholstein.local.ui;

import static de.larssh.utils.javafx.JavaFxUtils.alertOnException;

import java.io.IOException;
import java.io.UncheckedIOException;

import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {
	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	public void start(@Nullable final Stage primaryStage) {
		alertOnException(primaryStage, () -> {
			try {
				new MainController(this, Nullables.orElseThrow(primaryStage));
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
}
