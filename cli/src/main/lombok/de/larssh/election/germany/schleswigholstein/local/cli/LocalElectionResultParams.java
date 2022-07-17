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
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFiles;
import de.larssh.utils.Finals;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import picocli.CommandLine.Option;

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
				try (Reader reader = Files.newBufferedReader(resultPath.getValue())) {
					result = result.add(PollingStationResultFiles.read(election, pollingStation, reader));
				}
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
	}
}
