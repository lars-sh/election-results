package de.larssh.election.germany.schleswigholstein.ui;

import java.io.IOException;

import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Main extends Application {
	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	public void start(@Nullable final Stage primaryStage) throws IOException {
		final Stage stage = Nullables.orElseThrow(primaryStage);

		final Parent parent = FXMLLoader.load(getClass().getResource("ui.fxml"));
		stage.setScene(new Scene(parent));
		stage.show();
	}
}
