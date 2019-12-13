package de.larssh.utils.javafx;

import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.control.Spinner;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SafeStringConverter<T> extends StringConverter<T> {
	Spinner<T> spinner;

	StringConverter<T> converter;

	@Override
	public T fromString(@Nullable final String value) {
		try {
			return Nullables.orElseGet(getConverter().fromString(value), getSpinner()::getValue);
		} catch (@SuppressWarnings("unused") final Exception e) {
			return getSpinner().getValue();
		}
	}

	@Override
	public String toString(@Nullable final T object) {
		return getConverter().toString(object);
	}
}
