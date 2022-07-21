package de.larssh.election.germany.schleswigholstein.local.cli;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import de.larssh.election.germany.schleswigholstein.ElectionException;
import de.larssh.election.germany.schleswigholstein.local.LocalDistrict;
import de.larssh.election.germany.schleswigholstein.local.LocalElection;
import de.larssh.election.germany.schleswigholstein.local.LocalElectionResult;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFileLineParseException;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFileParseException;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFiles;
import de.larssh.utils.Nullables;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/**
 * A picocli compatible parameter set to load election results via command line
 * options.
 */
@RequiredArgsConstructor
public class LocalElectionResultParameter {
	/**
	 * Path to the election data
	 */
	@NonFinal
	@Nullable
	@Option(names = { "-e", "--election" },
			paramLabel = "<Path>",
			required = true,
			description = "Path to the election data")
	Path electionPath = null;

	/**
	 * Scale (decimal places) of Sainte Laguë values
	 */
	@NonFinal
	@Option(names = { "-s", "--sainte-lague-scale" }, description = "Scale (decimal places) of Sainte Laguë values")
	int sainteLagueScale = 2;

	/**
	 * Any number of key-value pairs with the polling station as key and a path to
	 * the corresponding result file as value
	 */
	@NonFinal
	@Option(names = { "-R", "--result" },
			paramLabel = "<Polling Station>=<Path>",
			description = "Any number of key-value pairs with the polling station as key and a path to the corresponding result file as value")
	Map<String, Path> resultPaths = emptyMap();

	/**
	 * Current {@link CommandSpec} instance
	 */
	@Spec
	@NonFinal
	@Nullable
	CommandSpec commandSpec = null;

	private PrintWriter getStandardErrorWriter() {
		return Nullables.orElseThrow(commandSpec).commandLine().getErr();
	}

	/**
	 * Dummy to avoid the IDE to mark some fields as {@code final}.
	 */
	@SuppressWarnings("unused")
	private void nonFinalDummy() {
		electionPath = null;
		sainteLagueScale = 2;
		resultPaths = emptyMap();
		commandSpec = null;
	}

	/**
	 * Reads all results specified by {@link #resultPaths} and merges them all
	 * together.
	 *
	 * @return one result containing multiple results
	 * @throws IOException on IO error
	 */
	public LocalElectionResult read() throws IOException {
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
	}

	/**
	 * Reads a single election result by {@code path}.
	 *
	 * @param election       the election
	 * @param pollingStation the polling station to read results for
	 * @param path           the path to the polling station results file to load
	 * @return the loaded result
	 * @throws IOException on IO error
	 */
	@SuppressWarnings({ "checkstyle:SuppressWarnings", "resource" })
	private LocalElectionResult readSingleResult(final LocalElection election,
			final LocalPollingStation pollingStation,
			final Path path) throws IOException {
		try (Reader reader = Files.newBufferedReader(path)) {
			return PollingStationResultFiles.read(election, pollingStation, reader);
		} catch (final PollingStationResultFileParseException e) {
			for (final PollingStationResultFileLineParseException lineException : e
					.getSuppressedLineParseExceptions()) {
				getStandardErrorWriter().println(String.format("Line %d of \"%s\": %-70s | %s",
						lineException.getLineNumber(),
						path.getFileName().toString(),
						lineException.getMessage(),
						lineException.getLineContent()));

				final Throwable cause = lineException.getCause();
				if (cause != null && !(cause instanceof ElectionException)) {
					cause.printStackTrace(getStandardErrorWriter());
				}
			}
			return e.getIncompleteResult();
		}
	}
}
