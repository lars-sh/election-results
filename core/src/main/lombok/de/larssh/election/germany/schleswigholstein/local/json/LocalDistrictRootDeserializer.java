package de.larssh.election.germany.schleswigholstein.local.json;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

import de.larssh.election.germany.schleswigholstein.local.LocalDistrict;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrictRoot;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrictType;
import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class LocalDistrictRootDeserializer extends JacksonDeserializer<LocalDistrictRoot> {
	@Override
	public LocalDistrictRoot deserialize(@Nullable final JsonParser nullableParser,
			@SuppressWarnings("unused") @Nullable final DeserializationContext context) throws IOException {
		return Nullables.orElseThrow(nullableParser).readValueAs(ParsableLocalDistrictRoot.class).asLocalDistrictRoot();
	}

	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalDistrictRoot {
		final String name;

		final Set<ParsableLocalDistrict> children;

		final LocalDistrictType type;

		public LocalDistrictRoot asLocalDistrictRoot() {
			final LocalDistrictRoot district = new LocalDistrictRoot(getName(), getType());
			getChildren().forEach(child -> {
				final LocalDistrict localDistrict = district.createChild(child.getName());
				child.getChildren().forEach(pollingStation -> localDistrict.createChild(pollingStation.getName()));
			});
			return district;
		}
	}

	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalDistrict {
		final String name;

		final Set<ParsableLocalPollingStation> children;
	}

	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalPollingStation {
		final String name;
	}
}
