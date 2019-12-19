package de.larssh.election.germany.schleswigholstein.local.json;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.umd.cs.findbugs.annotations.Nullable;

public abstract class JacksonDeserializer<T> extends JsonDeserializer<T> {
	public JacksonDeserializer() {
		// nothing to initialize
	}

	protected int getInteger(final JsonParser parser, final DeserializationContext context) throws IOException {
		if (parser.currentToken() == JsonToken.VALUE_NUMBER_INT) {
			return parser.getIntValue();
		}
		throw context.wrongTokenException(parser, handledType(), JsonToken.VALUE_NUMBER_INT, null);
	}

	protected String getString(final JsonParser parser, final DeserializationContext context) throws IOException {
		if (parser.currentToken() == JsonToken.VALUE_STRING) {
			return parser.getText();
		}
		throw context.wrongTokenException(parser, handledType(), JsonToken.VALUE_STRING, null);
	}

	@Nullable
	protected String readNextField(final JsonParser parser) throws IOException {
		parser.nextValue();
		return parser.currentToken() == JsonToken.END_ARRAY || parser.currentToken() == JsonToken.END_OBJECT
				? null
				: parser.currentName();
	}

	protected <V> V throwIfMissing(final Optional<V> value, final String fieldName, final JsonParser parser)
			throws JsonMappingException {
		return value.orElseThrow(
				() -> JsonMappingException.from(parser, String.format("Missing field \"%s\".", fieldName), null));
	}
}
