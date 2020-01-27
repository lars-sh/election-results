package de.larssh.utils.javafx;

import java.io.IOException;
import java.util.regex.Pattern;

import de.larssh.utils.text.Strings;
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
	private static final Pattern CLASS_SUFFIX_CONTROLLER_PATTERN = Pattern.compile("Controller$");

	Application application;

	Stage stage;

	public Parent loadFxml() throws IOException {
		final FXMLLoader loader = new FXMLLoader(getClass().getResource(
				Strings.replaceFirst(getClass().getSimpleName(), CLASS_SUFFIX_CONTROLLER_PATTERN, ".fxml")));
		loader.setController(this);
		return loader.load();
	}

	@FXML
	protected abstract void initialize();
}
