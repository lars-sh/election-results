package de.larssh.utils.javafx;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class Controller {
	Application application;

	Stage stage;

	public Parent loadFxml() throws IOException {
		final FXMLLoader loader = new FXMLLoader(
				getClass().getResource(getClass().getSimpleName().replaceFirst("Controller$", ".fxml")));
		loader.setController(this);
		return loader.load();
	}

	@FXML
	protected abstract void initialize();
}
