package de.larssh.election.germany.schleswigholstein.local.ui;

import java.io.IOException;
import java.time.LocalDate;

import de.larssh.election.germany.schleswigholstein.local.LocalDistrictSuper;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrictType;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

@Getter
public class Main extends Application {
	public static void main(final String[] args) {
		launch(args);
	}

	UiController uiController;

	public Main() throws IOException {
		uiController = new UiController(this);
	}

	@Override
	public void start(@Nullable final Stage primaryStage) throws IOException {
		final Stage stage = Nullables.orElseThrow(primaryStage);
		stage.setScene(new Scene(uiController.loadFxml()));
		stage.show();

		final LocalDistrictSuper district
				= new LocalDistrictSuper("Gemeinde Rethwisch", LocalDistrictType.KREISANGEHOERIGE_GEMEINDE);
		final LocalElection election = new LocalElection(district, LocalDate.of(2019, 11, 2), "Testwahl", 2);
		uiController.getElectionController().setElection(election);
	}
}
