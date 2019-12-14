package de.larssh.utils.javafx;

import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JavaFxUtils {
	public static <T> void initializeEditableSpinner(final Spinner<T> spinner,
			final SpinnerValueFactory<T> spinnerValueFactory) {
		spinner.setValueFactory(spinnerValueFactory);

		// String Converter
		final StringConverter<T> stringConverter
				= new SafeStringConverter<>(spinner, spinnerValueFactory.getConverter());
		spinnerValueFactory.setConverter(stringConverter);

		// Text Formatter
		final TextFormatter<T> textFormatter = new TextFormatter<>(stringConverter);
		textFormatter.valueProperty().bindBidirectional(spinnerValueFactory.valueProperty());
		spinner.getEditor().setTextFormatter(textFormatter);
	}

	public static <T extends Throwable> T showUnexpectedError(@Nullable final Stage stage, final T throwable) {
		final Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Fehler");
		alert.setHeaderText("Unerwarteter Fehler");
		alert.setContentText(throwable.getLocalizedMessage());
		alert.initOwner(stage);
		alert.show();

		return throwable;
	}
}
