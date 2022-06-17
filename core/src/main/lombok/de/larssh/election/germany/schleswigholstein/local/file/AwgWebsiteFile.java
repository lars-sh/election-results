package de.larssh.election.germany.schleswigholstein.local.file;

import static de.larssh.utils.Collectors.toLinkedHashSet;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.local.LocalBallot;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrict;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalNomination;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationType;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.utils.OptionalInts;
import de.larssh.utils.text.Strings;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AwgWebsiteFile {
	private static String createPhpIdentifier(final String value) {
		return "'"
				+ Strings.toLowerCaseNeutral(value)
						.replace("ä", "ae")
						.replace("ö", "oe")
						.replace("ü", "ue")
						.replace("ß", "ss")
						.replaceAll("[^a-z0-9]+", "-")
						.replaceAll("(?:^[-0-9]+)|(?:-+$)", "")
				+ "'";
	}

	private static String createPhpString(final String value) {
		return "'"
				+ value.replace("\r", "\\r") //
						.replace("\n", "\\n")
						.replace("\\", "\\\\")
						.replace("'", "\\'")
				+ "'";
	}

	public static void write(final LocalElectionResult result, final Writer writer) throws IOException {
		new AwgWebsiteFileWriter(result, writer).write();
	}

	@RequiredArgsConstructor
	private static class AwgWebsiteFileWriter {
		LocalElectionResult result;

		Writer writer;

		private void write() throws IOException {
			final String wahlberechtigteOrNull
					= OptionalInts.mapToObj(result.getElection().getNumberOfEligibleVoters(), Integer::toString)
							.orElse("null");
			final String stimmzettelOrNull
					= OptionalInts.mapToObj(result.getNumberOfAllBallots(), Integer::toString).orElse("null");
			final long davonBriefwaehler = result.getBallots().stream().filter(LocalBallot::isPostalVote).count();
			final int ungueltigeStimmen = result.getNumberOfInvalidBallots();

			write("<?php\n");
			write("function data_%tY_get_meta() {\n", result.getElection().getDate());
			write("\treturn array(\n");
			write("\t\t'wahlberechtigte' => %s,\n", wahlberechtigteOrNull);
			write("\t\t'stimmzettel' => %s,\n", stimmzettelOrNull);
			write("\t\t'davon-briefwaehler' => %d,\n", davonBriefwaehler);
			write("\t\t'ungueltige-stimmen' => %d\n", ungueltigeStimmen);
			write("\t);\n");
			write("}\n");
			write("\n");
			write("function data_%tY_get_data() {\n", result.getElection().getDate());
			write("\t$return = array(\n");
			writeData();
			write("\t);\n");
			write("\t\n");
			write("\t$return['gesamt'] = array();\n");
			write("\tforeach ($return['klein-boden'] as $key => $value) {\n");
			write("\t\t$return['gesamt'][$key] = $value + $return['rethwischdorf'][$key];\n");
			write("\t}\n");
			write("\t\n");
			write("\treturn $return;\n");
			write("}\n");
			write("\n");
			write("function data_%tY_get_personen() {\n", result.getElection().getDate());
			write("\treturn array(\n");
			writePersonen();
			write("\t);\n");
			write("}\n");
			write("\n");
			write("function data_%tY_get_seats() {\n", result.getElection().getDate());
			write("\treturn array(");
			writeSeats();
			write(");\n");
			write("}\n");
			write("\n");
			write("function data_%tY_get_types() {\n", result.getElection().getDate());
			write("\treturn array(\n");
			write("\t\t'gesamt' => 'Gesamt',\n");
			writeTypes();
			write("\t);\n");
			write("}\n");
		}

		private void write(final String format, final Object... args) throws IOException {
			writer.write(Strings.format(format, args));
		}

		private void writeData() throws IOException {
			final Set<LocalPollingStation> pollingStations = result.getElection()
					.getDistrict()
					.getChildren()
					.stream()
					.map(LocalDistrict::getChildren)
					.flatMap(Set::stream)
					.collect(toLinkedHashSet());

			final AtomicBoolean isFirst = new AtomicBoolean(true);
			for (final LocalPollingStation pollingStation : pollingStations) {
				if (!isFirst.getAndSet(false)) {
					write(",\n");
				}
				write("\t\t%s => array(\n", createPhpIdentifier(pollingStation.getName()));
				writeDataOf(pollingStation);
				write("\t\t)");
			}
			write("\n");
		}

		private void writeDataOf(final LocalPollingStation pollingStation) throws IOException {
			write(result.filter(ballot -> ballot.getPollingStation().equals(pollingStation))
					.getNominationResults()
					.values()
					.stream()
					.filter(nominationResult -> nominationResult.getNomination()
							.getType() == LocalNominationType.DIRECT)
					.map(nominationResult -> String.format("\t\t\t%s => %d",
							createPhpIdentifier(nominationResult.getNomination().getPerson().getKey()),
							nominationResult.getNumberOfVotes()))
					.collect(Collectors.joining(",\n", "", "\n")));
		}

		private void writePersonen() throws IOException {
			final AtomicBoolean isFirst = new AtomicBoolean(true);
			for (final LocalNomination nomination : result.getElection().getNominations()) {
				if (!isFirst.getAndSet(false)) {
					write(",\n");
				}

				final String gruppierungOrNull = nomination.getParty()
						.map(Party::getShortName)
						.map(AwgWebsiteFile::createPhpIdentifier)
						.orElse("null");

				write("\t\t%s => array(\n", createPhpIdentifier(nomination.getPerson().getKey()));
				write("\t\t\t'gruppierung' => %s,\n", gruppierungOrNull);
				write("\t\t\t'nachname' => %s,\n", createPhpString(nomination.getPerson().getFamilyName()));
				write("\t\t\t'vorname' => %s\n", createPhpString(nomination.getPerson().getGivenName()));
				write("\t\t)");
			}
			write("\n");
		}

		private void writeSeats() throws IOException {
			write(result.getPartyResults()
					.values()
					.stream()
					.map(partyResult -> String.format("\t\t%s => %d",
							createPhpIdentifier(partyResult.getParty().getShortName()),
							partyResult.getNumberOfSeats()))
					.collect(Collectors.joining(",\n", "\n", "\n\t")));
		}

		private void writeTypes() throws IOException {
			write(result.getElection()
					.getDistrict()
					.getChildren()
					.stream()
					.map(LocalDistrict::getChildren)
					.flatMap(Set::stream)
					.map(pollingStation -> String.format("\t\t%s => %s",
							createPhpIdentifier(pollingStation.getName()),
							createPhpString(pollingStation.getName())))
					.collect(Collectors.joining(",\n", "", "\n")));
		}
	}
}
