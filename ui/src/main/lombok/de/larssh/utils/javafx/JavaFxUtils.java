package de.larssh.utils.javafx;

import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JavaFxUtils {
	@SuppressWarnings({
			"checkstyle:XIllegalCatchDefault",
			"PMD.AvoidCatchingGenericException",
			"PMD.AvoidPrintStackTrace" })
	@SuppressFBWarnings(
			value = { "IMC_IMMATURE_CLASS_PRINTSTACKTRACE", "INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE" },
			justification = "temporary solution only")
	public static void alertOnException(@Nullable final Stage stage, final Runnable runnable) {
		try {
			runnable.run();
		} catch (final Exception e) {
			final Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fehler");
			alert.setHeaderText("Unerwarteter Fehler");
			alert.setContentText(e.getLocalizedMessage());
			alert.initOwner(stage);
			alert.show();

			// TODO: Show throwable as part of the alert
			e.printStackTrace();
		}
	}

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

	@Getter
	@RequiredArgsConstructor
	@SuppressWarnings({ "checkstyle:XIllegalCatchDefault", "PMD.AvoidCatchingGenericException" })
	private static class SafeStringConverter<T> extends StringConverter<T> {
		Spinner<T> spinner;

		StringConverter<T> converter;

		@Override
		public T fromString(@Nullable final String value) {
			try {
				return Nullables.orElseGet(getConverter().fromString(value), getSpinner()::getValue);
			} catch (@SuppressWarnings("unused") final Exception e) {
				// Expecting any exception to be a parsing error
				return getSpinner().getValue();
			}
		}

		@Override
		public String toString(@Nullable final T object) {
			return getConverter().toString(object);
		}
	}
}
