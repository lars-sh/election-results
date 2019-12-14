package de.larssh.election.germany.schleswigholstein.local.ui;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.utils.Resources;
import de.larssh.utils.javafx.JavaFxUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.NonFinal;

@Getter
public class MainController extends MainUiController {
	private static final Manifest MANIFEST;

	static {
		try {
			MANIFEST = Resources.readManifest(MainController.class).orElseGet(() -> {
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

	private static String getManifestValue(final Name name) {
		return MANIFEST.getMainAttributes().get(name).toString();
	}

	LocalElectionController electionController;

	@NonFinal
	Optional<Path> path = Optional.empty();

	FileChooser fileChooser = new FileChooser();

	@Getter(AccessLevel.PRIVATE)
	ObjectMapper objectMapper = new ObjectMapper();

	public MainController(final MainApplication application, final Stage stage) {
		super(application, stage);

		electionController = new LocalElectionController(this);
		getStage().setScene(new Scene(loadFxml()));
		getStage().show();
	}

	@FXML
	private void onOpen(final ActionEvent event) {
		final File file = fileChooser.showOpenDialog(getStage());
		if (file != null) {
			path = Optional.of(file.toPath());
			try {
				final LocalElection election = getObjectMapper().readValue(
						new String(Files.readAllBytes(path.get()), StandardCharsets.UTF_8),
						LocalElection.class);
				getElectionController().setElection(election);
			} catch (final IOException e) {
				JavaFxUtils.showUnexpectedError(getStage(), e);
			}
		}
	}

	@FXML
	private void onSave(final ActionEvent event) {
		if (path.isPresent()) {
			try {
				Files.write(path.get(),
						getObjectMapper().writerWithDefaultPrettyPrinter()
								.writeValueAsString(getElectionController().getElection())
								.getBytes(StandardCharsets.UTF_8));
			} catch (final IOException e) {
				JavaFxUtils.showUnexpectedError(getStage(), e);
			}
		} else {
			onSaveAs(event);
		}
	}

	@FXML
	private void onSaveAs(final ActionEvent event) {
		final File file = fileChooser.showSaveDialog(getStage());
		if (file != null) {
			path = Optional.of(file.toPath());
			onSave(event);
		}
	}

	@FXML
	private void onClose(@SuppressWarnings("unused") final ActionEvent event) {
		reset(); // TODO: ask the user
	}

	@FXML
	private void onExit(@SuppressWarnings("unused") final ActionEvent event) {
		Platform.exit(); // TODO: ask the user!
	}

	@FXML
	private void onAbout(@SuppressWarnings("unused") final ActionEvent event) {
		final Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Über");
		alert.setHeaderText(getApplicationTitle());
		alert.setContentText("by " + getManifestValue(Name.IMPLEMENTATION_VENDOR));
		alert.initOwner(getStage());
		alert.show();
	}

	private String getApplicationTitle() {
		return String.format("%s %s",
				getManifestValue(Name.IMPLEMENTATION_TITLE),
				getManifestValue(Name.IMPLEMENTATION_VERSION));
	}

	@FXML
	private void initialize() {
		// TODO: window title

		getElection().setContent(getElectionController().loadFxml());

		reset();
	}

	public void reset() {
		path = Optional.empty();
		getElectionController().reset();
	}
}
