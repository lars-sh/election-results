package de.larssh.election.germany.schleswigholstein.local.json;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;

import de.larssh.election.germany.schleswigholstein.local.LocalDistrictRoot;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;

public class LocalElectionDeserializer extends JacksonDeserializer<LocalElection> {
	@Override
	@SuppressWarnings("resource")
	public LocalElection deserialize(@Nullable final JsonParser nullableParser,
			@Nullable final DeserializationContext nullableContext) throws IOException {
		final JsonParser parser = Nullables.orElseThrow(nullableParser);
		final DeserializationContext context = Nullables.orElseThrow(nullableContext);

		if (!parser.isExpectedStartObjectToken()) {
			throw context.wrongTokenException(parser, handledType(), JsonToken.START_OBJECT, null);
		}

		Optional<LocalDate> date = Optional.empty();
		Optional<String> name = Optional.empty();
		int sainteLagueScale = LocalElection.SAINTE_LAGUE_SCALE_DEFAULT;
		Optional<LocalDistrictRoot> district = Optional.empty();

		for (String fieldName = readNextField(parser); fieldName != null; fieldName = readNextField(parser)) {
			// TODO: Fail on wrong types
			switch (fieldName) {
			case "date":
				date = Optional.of(LocalDate.parse(getString(parser, context)));
				break;
			case "name":
				name = Optional.of(getString(parser, context));
				break;
			case "sainteLagueScale":
				sainteLagueScale = getInteger(parser, context);
				break;
			case "districts":
				district = Optional.of(parser.readValueAs(LocalDistrictRoot.class));
				break;
			case "parties":
				// TODO
				break;
			case "nominations":
				// TODO
				break;
			case "population":
				// TODO
				break;
			case "numberOfEligibleVoters":
				// TODO
				break;
			default:
				// Ignore unknown fields
			}
		}

		final LocalElection election = new LocalElection(throwIfMissing(district, "district", parser),
				throwIfMissing(date, "date", parser),
				throwIfMissing(name, "name", parser),
				sainteLagueScale);
		// TOOD: population
		// TODO: number of eligible voters
		return election;
	}
}
