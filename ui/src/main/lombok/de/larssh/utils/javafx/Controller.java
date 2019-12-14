package de.larssh.utils.javafx;

import java.io.IOException;
import java.io.UncheckedIOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class Controller {
	Application application;

	Stage stage;

	public Parent loadFxml() {
		final FXMLLoader loader = new FXMLLoader(
				getClass().getResource(getClass().getSimpleName().replaceFirst("Controller$", ".fxml")));
		loader.setController(this);
		try {
			return loader.load();
		} catch (final IOException e) {
			throw new UncheckedIOException(JavaFxUtils.showUnexpectedError(getStage(), e));
		}
	}
}
