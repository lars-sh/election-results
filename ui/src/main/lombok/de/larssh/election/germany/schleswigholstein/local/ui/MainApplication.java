package de.larssh.election.germany.schleswigholstein.local.ui;

import static de.larssh.utils.javafx.JavaFxUtils.alertOnException;

import java.io.IOException;
import java.io.UncheckedIOException;

import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@SuppressWarnings("checkstyle:UncommentedMain")
public class MainApplication extends Application {
	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	@SuppressFBWarnings(value = "EXS_EXCEPTION_SOFTENING_NO_CHECKED",
			justification = "runtime failed if the main controller cannot be initialized")
	public void start(@Nullable final Stage primaryStage) {
		alertOnException(primaryStage, () -> {
			final MainController mainController = new MainController(this, Nullables.orElseThrow(primaryStage));
			try {
				mainController.show();
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
}
