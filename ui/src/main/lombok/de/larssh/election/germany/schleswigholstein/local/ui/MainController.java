package de.larssh.election.germany.schleswigholstein.local.ui;

import static de.larssh.utils.javafx.JavaFxUtils.alertOnException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import com.fasterxml.jackson.databind.ObjectWriter;

import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.utils.Nullables;
import de.larssh.utils.Resources;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import lombok.Getter;

@Getter
public class MainController extends MainUiController {
	private static final Manifest MANIFEST = readManifest();

	private static final ObjectWriter OBJECT_WRITER = LocalElection.createJacksonObjectWriter();

	private static String getManifestValue(final Name name) {
		return MANIFEST.getMainAttributes().get(name).toString();
	}

	@SuppressFBWarnings(value = "EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS",
			justification = "failing to read the manifest is a runtime exception")
	private static Manifest readManifest() {
		try {
			return Resources.readManifest(MainController.class).orElseGet(() -> {
				final Manifest manifest = new Manifest();
				manifest.getMainAttributes().put(Name.IMPLEMENTATION_TITLE, "Election Results UI");
				manifest.getMainAttributes().put(Name.IMPLEMENTATION_VERSION, "Development");
				manifest.getMainAttributes().put(Name.IMPLEMENTATION_VENDOR, "Lars Knickrehm");
				return manifest;
			});
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	LocalElectionController electionController;

	private ObjectProperty<Optional<Path>> path = new SimpleObjectProperty<>(Optional.empty());

	FileChooser fileChooser;

	public MainController(final MainApplication application, final Stage stage) {
		super(application, stage);

		electionController = new LocalElectionController(this);

		fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Wahl-Definitionsdateien (*.json)", "*.json"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Alle Dateien (*.*)", "*.*"));
	}

	private String getApplicationTitle() {
		return String.format("%s %s",
				getManifestValue(Name.IMPLEMENTATION_TITLE),
				getManifestValue(Name.IMPLEMENTATION_VERSION));
	}

	@Override
	protected void initialize() {
		alertOnException(getStage(), () -> {
			getPath().addListener((observer, oldValue, newValue) -> updateStageTitle(newValue));

			try {
				getPane().getChildren().add(getElectionController().loadFxml());
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}

			reset();
		});
	}

	@FXML
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@SuppressFBWarnings(value = { "UP_UNUSED_PARAMETER", "UPM_UNCALLED_PRIVATE_METHOD" },
			justification = "JavaFX event")
	private void onOpen(@SuppressWarnings("unused") final ActionEvent event) {
		alertOnException(getStage(), () -> {
			final File file = fileChooser.showOpenDialog(getStage());
			if (file != null) {
				Nullables.ifNonNull(file.getParentFile(), fileChooser::setInitialDirectory);
				try (Reader reader = Files.newBufferedReader(file.toPath())) {
					getElectionController().setElection(LocalElection.fromJson(reader));
				} catch (final IOException e) {
					throw new UncheckedIOException(e);
				}
				getPath().setValue(Optional.of(file.toPath()));
			}
		});
	}

	@FXML
	private void onSave(final ActionEvent event) {
		alertOnException(getStage(), () -> {
			if (getPath().getValue().isPresent()) {
				try {
					OBJECT_WRITER.writeValue(getPath().getValue().get().toFile(),
							getElectionController().getElection());
				} catch (final IOException e) {
					throw new UncheckedIOException(e);
				}
			} else {
				onSaveAs(event);
			}
		});
	}

	@FXML
	private void onSaveAs(final ActionEvent event) {
		alertOnException(getStage(), () -> {
			final File file = fileChooser.showSaveDialog(getStage());
			if (file != null) {
				Nullables.ifNonNull(file.getParentFile(), fileChooser::setInitialDirectory);
				getPath().setValue(Optional.of(file.toPath()));
				onSave(event);
			}
		});
	}

	@FXML
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@SuppressFBWarnings(value = { "UP_UNUSED_PARAMETER", "UPM_UNCALLED_PRIVATE_METHOD" },
			justification = "JavaFX event")
	private void onClose(@SuppressWarnings("unused") final ActionEvent event) {
		alertOnException(getStage(), () -> {
			final Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Schließen");
			alert.setHeaderText("Möchten Sie die Datei schließen ohne zu speichern?");
			alert.setContentText("Beim Schließen gehen ungespeicherte Daten unwiderruflich verloren.");
			alert.initOwner(getStage());
			if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
				reset();
			}
		});
	}

	@FXML
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@SuppressFBWarnings(value = { "UP_UNUSED_PARAMETER", "UPM_UNCALLED_PRIVATE_METHOD" },
			justification = "JavaFX event")
	private void onExit(@SuppressWarnings("unused") final ActionEvent event) {
		alertOnException(getStage(), () -> {
			final Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Beenden");
			alert.setHeaderText("Möchten Sie beenden ohne zu speichern?");
			alert.setContentText("Beim Beenden gehen ungespeicherte Daten unwiderruflich verloren.");
			alert.initOwner(getStage());
			if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
				Platform.exit();
			}
		});
	}

	@FXML
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@SuppressFBWarnings(value = { "UP_UNUSED_PARAMETER", "UPM_UNCALLED_PRIVATE_METHOD" },
			justification = "JavaFX event")
	private void onAbout(@SuppressWarnings("unused") final ActionEvent event) {
		alertOnException(getStage(), () -> {
			final Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Über");
			alert.setHeaderText(getApplicationTitle());
			alert.setContentText("by " + getManifestValue(Name.IMPLEMENTATION_VENDOR));
			alert.initOwner(getStage());
			alert.show();
		});
	}

	public void reset() {
		updateStageTitle(Optional.empty());
		getPath().setValue(Optional.empty());
		getElectionController().reset();
	}

	public void show() throws IOException {
		getStage().setScene(new Scene(loadFxml()));
		getStage().show();
	}

	private void updateStageTitle(final Optional<Path> path) {
		getStage().setTitle(getApplicationTitle() + path.map(p -> " - " + p.toString()).orElse(""));
	}
}
