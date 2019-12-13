package de.larssh.election.germany.schleswigholstein.local.ui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public interface Controller {
	default Parent loadFxml() throws IOException {
		final FXMLLoader loader = new FXMLLoader(
				getClass().getResource(getClass().getSimpleName().replaceFirst("Controller$", ".fxml")));
		loader.setController(this);
		return loader.load();
	}
}
