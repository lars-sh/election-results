package de.larssh.utils.javafx;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
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
}
