package de.larssh.election.germany.schleswigholstein.local.json;

import java.io.IOException;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.election.germany.schleswigholstein.local.LocalNomination;
import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;

public class LocalElectionSerializer extends JsonSerializer<LocalElection> {
	@Override
	public void serialize(@Nullable final LocalElection election,
			@Nullable final JsonGenerator nullableGenerator,
			@SuppressWarnings("unused") @Nullable final SerializerProvider serializerProvider) throws IOException {
		@SuppressWarnings("resource")
		final JsonGenerator generator = Nullables.orElseThrow(nullableGenerator);

		if (election == null) {
			generator.writeNull();
		} else {
			final Set<District<?>> collectedDistricts = collectDistricts(election.getDistrict());

			generator.writeStartObject();
			generator.writeObjectField("date", election.getDate().toString());
			generator.writeObjectField("name", election.getName());
			generator.writeObjectField("sainteLagueScale", election.getSainteLagueScale());
			generator.writeObjectField("districts", election.getDistrict());
			generator.writeObjectField("parties", election.getParties());
			generator.writeFieldName("nominations");
			writeNominations(generator, election);
			generator.writeFieldName("population");
			writePopulations(generator, election, collectedDistricts);
			generator.writeFieldName("numberOfEligibleVoters");
			writeNumbersOfEligibleVoters(generator, election, collectedDistricts);
			generator.writeEndObject();
		}
	}

	private Set<District<?>> collectDistricts(final District<?> district) {
		final Set<District<?>> districts = new TreeSet<>();
		districts.add(district);
		for (final District<?> child : district.getChildren()) {
			districts.addAll(collectDistricts(child));
		}
		return districts;
	}

	private void writeNominations(final JsonGenerator generator, final LocalElection election) throws IOException {
		generator.writeStartArray();
		for (final LocalNomination nomination : election.getNominations()) {
			generator.writeStartObject();
			generator.writeObjectField("district", nomination.getDistrict().getName());
			generator.writeObjectField("type", nomination.getType());
			generator.writeObjectField("party", nomination.getParty().map(Party::getShortName).orElse(null));
			generator.writeObjectField("person", nomination.getPerson());
			generator.writeEndObject();
		}
		generator.writeEndArray();
	}

	private void writePopulations(final JsonGenerator generator,
			final LocalElection election,
			final Set<District<?>> collectedDistricts) throws IOException {
		generator.writeStartObject();
		for (final District<?> district : collectedDistricts) {
			final OptionalInt population = election.getPopulation(district);
			if (population.isPresent()) {
				generator.writeObjectField(district.getName(), population.getAsInt());
			}
		}
		generator.writeEndObject();
	}

	private void writeNumbersOfEligibleVoters(final JsonGenerator generator,
			final LocalElection election,
			final Set<District<?>> collectedDistricts) throws IOException {
		generator.writeStartObject();
		for (final District<?> district : collectedDistricts) {
			final OptionalInt numberOfEligibleVoters = election.getNumberOfEligibleVoters(district);
			if (numberOfEligibleVoters.isPresent()) {
				generator.writeObjectField(district.getName(), numberOfEligibleVoters.getAsInt());
			}
		}
		generator.writeEndObject();
	}
}
