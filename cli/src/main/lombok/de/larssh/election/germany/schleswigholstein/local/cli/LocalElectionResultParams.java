package de.larssh.election.germany.schleswigholstein.local.cli;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrict;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFileLineParseException;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFileParseException;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFiles;
import de.larssh.utils.Finals;
import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@RequiredArgsConstructor
public class LocalElectionResultParams {
	@NonFinal
	@Nullable
	@Option(names = { "-e", "--election" },
			paramLabel = "<Path>",
			required = true,
			description = "Path to the election data.")
	Path electionPath = null;

	@NonFinal
	@Option(names = { "-s", "--sainte-lague-scale" }, description = "Scale (decimal places) of Sainte Laguë values")
	int sainteLagueScale = 2;

	@NonFinal
	@Option(names = { "-R", "--result" },
			paramLabel = "<Polling Station>=<Path>",
			description = "Any number of key-value pairs with the polling station as key and a path to the corresponding result file as value.")
	Map<String, Path> resultPaths = emptyMap();

	@Spec
	@NonFinal
	@Nullable
	CommandSpec commandSpec = null;

	Supplier<LocalElectionResult> result = Finals.lazy(() -> {
		try {
			// Read Election
			final LocalElection election;
			try (Reader reader = Files.newBufferedReader(this.electionPath)) {
				election = LocalElection.fromJson(reader);
			}

			// Read all Election Results
			LocalElectionResult result
					= new LocalElectionResult(election, 2, emptyMap(), emptySet(), emptySet(), emptyList());
			for (final Entry<String, Path> resultPath : resultPaths.entrySet()) {
				// Find Polling Station
				final LocalPollingStation pollingStation = election.getDistrict()
						.getChildren()
						.stream()
						.map(LocalDistrict::getChildren)
						.flatMap(Collection::stream)
						.filter(district -> resultPath.getKey().equals(district.getName()))
						.findAny()
						.orElseThrow(() -> new ElectionException("Cannot find a polling station named \"%s\".",
								resultPath.getKey()));

				// Read Election Results of Polling Station
				result = result.add(readSingleResult(election, pollingStation, resultPath.getValue()));
			}
			return result;
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	});

	public LocalElectionResult get() throws IOException {
		try {
			return result.get();
		} catch (final UncheckedIOException e) {
			throw e.getCause();
		}
	}

	@SuppressWarnings("unused")
	private void nonFinalDummy() {
		electionPath = null;
		sainteLagueScale = 2;
		resultPaths = emptyMap();
		commandSpec = null;
	}

	@SuppressWarnings("resource")
	private LocalElectionResult readSingleResult(final LocalElection election,
			final LocalPollingStation pollingStation,
			final Path path) throws IOException {
		try (Reader reader = Files.newBufferedReader(path)) {
			return PollingStationResultFiles.read(election, pollingStation, reader);
		} catch (final PollingStationResultFileParseException e) {
			for (final PollingStationResultFileLineParseException exception : e.getSuppressedLineParseExceptions()) {
				Nullables.orElseThrow(commandSpec)
						.commandLine()
						.getErr()
						.println(String.format("Line %d of %s: %s",
								exception.getLineNumber(),
								path.getFileName().toString(),
								exception.getMessage()));
			}
			return e.getIncompleteResult();
		}
	}
}
