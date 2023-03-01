package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.election.germany.schleswigholstein.local.LocalElectionTest.findNomination;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.DIRECT;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.DIRECT_BALANCE_SEAT;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.DIRECT_DRAW;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.LIST;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.LIST_DRAW;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.LIST_OVERHANG_SEAT;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.NOT_ELECTED;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFilesTest;
import de.larssh.utils.annotations.PackagePrivate;
import lombok.NoArgsConstructor;

/**
 * {@link LocalNominationResult}
 */
@PackagePrivate
@NoArgsConstructor
class LocalNominationResultTest {
	/**
	 * Executes {@link LocalElectionResult#getNominationResults()} and asserts the
	 * result for the expected number of results.
	 *
	 * @param softAssertions the {@link SoftAssertions} object to add assertions to
	 * @param result         the {@link LocalElectionResult}
	 * @return Wahlergebnis einzelner Bewerberinnen und Bewerber
	 */
	private static Map<LocalNomination, LocalNominationResult> getAndAssertNominationResults(
			final SoftAssertions softAssertions,
			final LocalElectionResult result) {
		final Map<LocalNomination, LocalNominationResult> nominationResults = result.getNominationResults();
		softAssertions.assertThat(nominationResults).hasSameSizeAs(result.getElection().getNominations());
		return nominationResults;
	}

	/**
	 * Executes {@link LocalElectionResult#getNominationResults()} and asserts the
	 * result for the expected number of results.
	 *
	 * <p>
	 * {@code assertListNominationsAsNotElected} can be specified to test if list
	 * nominations are not elected, which is the case as long as no balance seat is
	 * involved.
	 *
	 * @param softAssertions                    the {@link SoftAssertions} object to
	 *                                          add assertions to
	 * @param result                            the {@link LocalElectionResult}
	 * @param assertListNominationsAsNotElected {@code true} if list nominations
	 *                                          shall be asserted as not elected,
	 *                                          else {@code false}
	 * @return Wahlergebnis einzelner Bewerberinnen und Bewerber
	 */
	private static Map<LocalNomination, LocalNominationResult> getAndAssertNominationResults(
			final SoftAssertions softAssertions,
			final LocalElectionResult result,
			final boolean assertListNominationsAsNotElected) {
		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(softAssertions, result);
		if (assertListNominationsAsNotElected) {
			for (final LocalNominationResult nominationResult : result.getNominationResults().values()) {
				if (nominationResult.getNomination().getType() == LocalNominationType.LIST) {
					softAssertions.assertThat(nominationResult.getType()).isEqualTo(NOT_ELECTED);
				}
			}
		}
		return nominationResults;
	}

	/**
	 * Reads the result file of {@code classRelativePath} for Klein Boden and read
	 * all-zero data for Rethwischdorf
	 *
	 * @param election          the election to reference
	 * @param classRelativePath path to a class-relative text file
	 * @return the {@link LocalElectionResult}
	 */
	public static LocalElectionResult readResultKleinBoden(final LocalElection election, final Path classRelativePath) {
		final LocalElectionResult resultRethwischdorf = PollingStationResultFilesTest.readResult(election,
				LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF,
				Paths.get("../LocalNominationResult-all-zero-finished.txt"));
		return PollingStationResultFilesTest
				.readResult(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN, classRelativePath)
				.add(resultRethwischdorf);
	}

	/**
	 * Tests the result types in the case specified by the input data read using
	 * {@code testKind} evaluated against the supplied expected values.
	 *
	 * @param testKind                     the test kind, used for the file name
	 * @param expectedNominationResultType a function taking the currently tested
	 *                                     nomination result and returning the
	 *                                     expected nomination result type
	 * @param expectedCertainResultType    a function taking the currently tested
	 *                                     nomination result and returning the
	 *                                     expected certain result type
	 */
	private static void testResultTypesForAllNominations(final String testKind,
			final Function<LocalNominationResult, LocalNominationResultType> expectedNominationResultType,
			final Function<LocalNominationResult, Optional<LocalNominationResultType>> expectedCertainResultType) {
		final SoftAssertions softAssertions = new SoftAssertions();
		final LocalElectionResult result = readResultKleinBoden(LocalElectionTest.createElection(),
				Paths.get("../LocalNominationResult-" + testKind + ".txt"));

		for (final LocalNominationResult nominationResult : getAndAssertNominationResults(softAssertions, result)
				.values()) {
			softAssertions.assertThat(nominationResult.getType())
					.describedAs("Nomination Result Type of \"%s\" when \"%s\"",
							nominationResult.getNomination().getKey(),
							testKind)
					.isEqualTo(expectedNominationResultType.apply(nominationResult));

			softAssertions.assertThat(nominationResult.getCertainResultType())
					.describedAs("Certain Result Type of \"%s\" when \"%s\"",
							nominationResult.getNomination().getKey(),
							testKind)
					.isEqualTo(expectedCertainResultType.apply(nominationResult));
		}
		softAssertions.assertAll();
	}

	/**
	 * Test the Sainte Laguë value of the local district Rethwisch based on the
	 * official results from 2018
	 */
	@Test
	@PackagePrivate
	void testSainteLagueRethwisch() {
		final LocalElectionResult result = PollingStationResultFilesTest.readResultsRethwisch();
		final LocalElection election = result.getElection();

		final SoftAssertions softAssertions = new SoftAssertions();
		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(softAssertions, result, true);

		// @formatter:off
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Beck", "Karsten")).getSainteLagueValue()).contains(BigDecimal.valueOf(24036, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Behnk", "Sönke")).getSainteLagueValue()).contains(BigDecimal.valueOf(88133, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Bernhardt", "Christian")).getSainteLagueValue()).contains(BigDecimal.valueOf(12590, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Breede", "Rolf")).getSainteLagueValue()).contains(BigDecimal.valueOf(20486, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Johannes")).getSainteLagueValue()).contains(BigDecimal.valueOf(31480, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Volker")).getSainteLagueValue()).contains(BigDecimal.valueOf(157400, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Dohrendorf", "Martina")).getSainteLagueValue()).contains(BigDecimal.valueOf(13916, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Dohrendorf", "Thomas")).getSainteLagueValue()).contains(BigDecimal.valueOf(9560, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Efrom", "Joachim")).getSainteLagueValue()).contains(BigDecimal.valueOf(8435, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eggers", "Dirk")).getSainteLagueValue()).contains(BigDecimal.valueOf(52880, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ehlert", "Armin")).getSainteLagueValue()).contains(BigDecimal.valueOf(11429, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eick", "Ernst")).getSainteLagueValue()).contains(BigDecimal.valueOf(26667, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Feddern", "Axel")).getSainteLagueValue()).contains(BigDecimal.valueOf(7547, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Feddern", "Hartmut")).getSainteLagueValue()).contains(BigDecimal.valueOf(6829, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gräpel", "Carola")).getSainteLagueValue()).contains(BigDecimal.valueOf(20338, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Henning")).getSainteLagueValue()).contains(BigDecimal.valueOf(17489, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Jan-Hendrik")).getSainteLagueValue()).contains(BigDecimal.valueOf(52467, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Hartz", "Catrin")).getSainteLagueValue()).contains(BigDecimal.valueOf(11031, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Jögimar", "Helga")).getSainteLagueValue()).contains(BigDecimal.valueOf(16000, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Klein", "Erik")).getSainteLagueValue()).contains(BigDecimal.valueOf(15553, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kraus", "Michael")).getSainteLagueValue()).contains(BigDecimal.valueOf(28680, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kröger", "Dirk")).getSainteLagueValue()).contains(BigDecimal.valueOf(80000, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "König", "Eva-Maria")).getSainteLagueValue()).contains(BigDecimal.valueOf(13036, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kühn", "Steffen")).getSainteLagueValue()).contains(BigDecimal.valueOf(143400, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Motzkus", "Dietrich")).getSainteLagueValue()).contains(BigDecimal.valueOf(37771, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Poppinga", "Jens")).getSainteLagueValue()).contains(BigDecimal.valueOf(264400, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Sauer", "Joachim")).getSainteLagueValue()).contains(BigDecimal.valueOf(7273, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Schwarz", "Rupert")).getSainteLagueValue()).contains(BigDecimal.valueOf(17627, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Schöning", "Mathias")).getSainteLagueValue()).contains(BigDecimal.valueOf(15933, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Stapelfeldt", "Albert")).getSainteLagueValue()).contains(BigDecimal.valueOf(14309, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Topel", "Andreas")).getSainteLagueValue()).contains(BigDecimal.valueOf(11496, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Wahl", "Joachim")).getSainteLagueValue()).contains(BigDecimal.valueOf(47800, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Weger", "Marcel")).getSainteLagueValue()).contains(BigDecimal.valueOf(29378, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Winter", "Martin")).getSainteLagueValue()).contains(BigDecimal.valueOf(22486, 2));
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ziebarth", "Angelika")).getSainteLagueValue()).contains(BigDecimal.valueOf(8889, 2));
		// @formatter:on

		softAssertions.assertAll();
	}

	/**
	 * Tests the result types in case no ballot was evaluated and the election
	 * evaluation finished.
	 */
	@Test
	@PackagePrivate
	void testResultTypesForAllNominationsZeroFinished() {
		testResultTypesForAllNominations("all-zero-finished",
				nominationResult -> LocalNominationResultType.NOT_ELECTED,
				nominationResult -> Optional.of(LocalNominationResultType.NOT_ELECTED));
	}

	/**
	 * Tests the result types in case no ballot was evaluated and the election
	 * evaluation is partially done.
	 */
	@Test
	@PackagePrivate
	void testResultTypesForAllNominationsZeroPartiallyDone() {
		testResultTypesForAllNominations("all-zero-partially-done",
				nominationResult -> LocalNominationResultType.NOT_ELECTED,
				nominationResult -> Optional.empty());
	}

	/**
	 * Tests the result types in case no ballot was evaluated and the election
	 * evaluation is not started.
	 */
	@Test
	@PackagePrivate
	void testResultTypesForAllNominationsZeroNotStarted() {
		testResultTypesForAllNominations("all-zero-not-started",
				nominationResult -> LocalNominationResultType.NOT_ELECTED,
				nominationResult -> Optional.empty());
	}

	/**
	 * Tests the result types in case exactly the same number of votes were given to
	 * all nominations and the election evaluation is finished.
	 */
	@Test
	@PackagePrivate
	void testResultTypesForAllNominationsOneFinished() {
		testResultTypesForAllNominations("all-one-finished",
				nominationResult -> nominationResult.getNomination().getType() == LocalNominationType.DIRECT
						? LocalNominationResultType.DIRECT_DRAW
						: LocalNominationResultType.NOT_ELECTED,
				nominationResult -> nominationResult.getType() == DIRECT_DRAW || nominationResult.getType() == LIST_DRAW
						? Optional.empty()
						: Optional.of(nominationResult.getType()));
	}

	/**
	 * Tests the result types in case exactly the same number of votes were given to
	 * all nominations and the election evaluation is partially done.
	 */
	@Test
	@PackagePrivate
	void testResultTypesForAllNominationsOnePartiallyDone() {
		testResultTypesForAllNominations("all-one-partially-done",
				nominationResult -> nominationResult.getNomination().getType() == LocalNominationType.DIRECT
						? LocalNominationResultType.DIRECT_DRAW
						: LocalNominationResultType.NOT_ELECTED,
				nominationResult -> Optional.empty());
	}

	/**
	 * Tests the result types in case exactly the same number of votes were given to
	 * all nominations and the election evaluation is not started.
	 */
	@Test
	@PackagePrivate
	void testResultTypesForAllNominationsOneNotStarted() {
		testResultTypesForAllNominations("all-one-not-started",
				nominationResult -> nominationResult.getNomination().getType() == LocalNominationType.DIRECT
						? LocalNominationResultType.DIRECT_DRAW
						: LocalNominationResultType.NOT_ELECTED,
				nominationResult -> Optional.empty());
	}

	/**
	 * Test types in case of balance and overhang seats
	 */
	@Test
	@PackagePrivate
	void testTypeBalanceAndOverhangSeats() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result
				= readResultKleinBoden(election, Paths.get("../LocalNominationResult-balance-and-overhang-seats.txt"));

		final SoftAssertions softAssertions = new SoftAssertions();
		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(softAssertions, result);

		// @formatter:off
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Beck", "Karsten")).getType()).isEqualTo(LIST_OVERHANG_SEAT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Behnk", "Sönke")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Bernhardt", "Christian")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Breede", "Rolf")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Johannes")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Volker")).getType()).isEqualTo(LIST_OVERHANG_SEAT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Dohrendorf", "Martina")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Dohrendorf", "Thomas")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Efrom", "Joachim")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eggers", "Dirk")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ehlert", "Armin")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eick", "Ernst")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Feddern", "Axel")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Feddern", "Hartmut")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gräpel", "Carola")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Henning")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Jan-Hendrik")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Hartz", "Catrin")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Jögimar", "Helga")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Klein", "Erik")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kraus", "Michael")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kröger", "Dirk")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "König", "Eva-Maria")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kühn", "Steffen")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Motzkus", "Dietrich")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Poppinga", "Jens")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Sauer", "Joachim")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Schwarz", "Rupert")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Schöning", "Mathias")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Stapelfeldt", "Albert")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Topel", "Andreas")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Wahl", "Joachim")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Weger", "Marcel")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Winter", "Martin")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ziebarth", "Angelika")).getType()).isEqualTo(DIRECT_BALANCE_SEAT);
		// @formatter:on

		softAssertions.assertAll();
	}

	/**
	 * Test types in case of partially closed draws in case there are less list draw
	 * results than list positions
	 */
	@Test
	@PackagePrivate
	void testTypePartiallyClosedDraws1() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult resultWithoutDrawResults
				= readResultKleinBoden(election, Paths.get("../LocalNominationResult-draws.txt"));
		final LocalElectionResult result = new LocalElectionResult(election,
				2,
				emptyMap(),
				new HashSet<>(asList(findNomination(election, "Böttger", "Johannes"),
						findNomination(election, "Eggers", "Dirk"))),
				singleton(findNomination(election, "Beck", "Karsten")),
				resultWithoutDrawResults.getBallots());

		final SoftAssertions softAssertions = new SoftAssertions();
		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(softAssertions, result, true);

		// @formatter:off
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Beck", "Karsten")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Behnk", "Sönke")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Breede", "Rolf")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Johannes")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Volker")).getType()).isEqualTo(LIST_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eggers", "Dirk")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ehlert", "Armin")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eick", "Ernst")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Henning")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Jan-Hendrik")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Jögimar", "Helga")).getType()).isEqualTo(LIST_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kraus", "Michael")).getType()).isEqualTo(LIST_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kröger", "Dirk")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "König", "Eva-Maria")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kühn", "Steffen")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Motzkus", "Dietrich")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Poppinga", "Jens")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Sauer", "Joachim")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Schöning", "Mathias")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Stapelfeldt", "Albert")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Wahl", "Joachim")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Weger", "Marcel")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Winter", "Martin")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ziebarth", "Angelika")).getType()).isEqualTo(NOT_ELECTED);
		// @formatter:on

		softAssertions.assertAll();
	}

	/**
	 * Test types in case of partially closed draws in case there are less direct
	 * draw results than direct positions
	 */
	@Test
	@PackagePrivate
	void testTypePartiallyClosedDraws2() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult resultWithoutDrawResults
				= readResultKleinBoden(election, Paths.get("../LocalNominationResult-draws.txt"));
		final LocalElectionResult result = new LocalElectionResult(election,
				2,
				emptyMap(),
				singleton(findNomination(election, "Böttger", "Johannes")),
				new HashSet<>(asList(findNomination(election, "Beck", "Karsten"),
						findNomination(election, "Böttger", "Volker"),
						findNomination(election, "Jögimar", "Helga"))),
				resultWithoutDrawResults.getBallots());

		final SoftAssertions softAssertions = new SoftAssertions();
		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(softAssertions, result, true);

		// @formatter:off
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Beck", "Karsten")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Behnk", "Sönke")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Breede", "Rolf")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Johannes")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Volker")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eggers", "Dirk")).getType()).isEqualTo(DIRECT_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ehlert", "Armin")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eick", "Ernst")).getType()).isEqualTo(DIRECT_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Henning")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Jan-Hendrik")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Jögimar", "Helga")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kraus", "Michael")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kröger", "Dirk")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "König", "Eva-Maria")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kühn", "Steffen")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Motzkus", "Dietrich")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Poppinga", "Jens")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Sauer", "Joachim")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Schöning", "Mathias")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Stapelfeldt", "Albert")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Wahl", "Joachim")).getType()).isEqualTo(DIRECT_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Weger", "Marcel")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Winter", "Martin")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ziebarth", "Angelika")).getType()).isEqualTo(NOT_ELECTED);
		// @formatter:on

		softAssertions.assertAll();
	}

	/**
	 * Test types in case of completely open draws
	 */
	@Test
	@PackagePrivate
	void testTypeOpenDraws() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result
				= readResultKleinBoden(election, Paths.get("../LocalNominationResult-draws.txt"));

		final SoftAssertions softAssertions = new SoftAssertions();
		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(softAssertions, result, true);

		// @formatter:off
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Beck", "Karsten")).getType()).isEqualTo(LIST_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Behnk", "Sönke")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Breede", "Rolf")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Johannes")).getType()).isEqualTo(DIRECT_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Volker")).getType()).isEqualTo(LIST_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eggers", "Dirk")).getType()).isEqualTo(DIRECT_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ehlert", "Armin")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eick", "Ernst")).getType()).isEqualTo(DIRECT_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Henning")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Jan-Hendrik")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Jögimar", "Helga")).getType()).isEqualTo(LIST_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kraus", "Michael")).getType()).isEqualTo(LIST_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kröger", "Dirk")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "König", "Eva-Maria")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kühn", "Steffen")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Motzkus", "Dietrich")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Poppinga", "Jens")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Sauer", "Joachim")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Schöning", "Mathias")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Stapelfeldt", "Albert")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Wahl", "Joachim")).getType()).isEqualTo(DIRECT_DRAW);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Weger", "Marcel")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Winter", "Martin")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ziebarth", "Angelika")).getType()).isEqualTo(NOT_ELECTED);
		// @formatter:on

		softAssertions.assertAll();
	}

	/**
	 * Test types using results of Rethwisch
	 */
	@Test
	@PackagePrivate
	void testTypeRethwisch() {
		final LocalElectionResult result = PollingStationResultFilesTest.readResultsRethwisch();
		final LocalElection election = result.getElection();

		final SoftAssertions softAssertions = new SoftAssertions();
		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(softAssertions, result, true);

		// @formatter:off
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Beck", "Karsten")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Behnk", "Sönke")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Bernhardt", "Christian")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Breede", "Rolf")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Johannes")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Volker")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Dohrendorf", "Martina")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Dohrendorf", "Thomas")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Efrom", "Joachim")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eggers", "Dirk")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ehlert", "Armin")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eick", "Ernst")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Feddern", "Axel")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Feddern", "Hartmut")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gräpel", "Carola")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Henning")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Jan-Hendrik")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Hartz", "Catrin")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Jögimar", "Helga")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Klein", "Erik")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kraus", "Michael")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kröger", "Dirk")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "König", "Eva-Maria")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kühn", "Steffen")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Motzkus", "Dietrich")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Poppinga", "Jens")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Sauer", "Joachim")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Schwarz", "Rupert")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Schöning", "Mathias")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Stapelfeldt", "Albert")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Topel", "Andreas")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Wahl", "Joachim")).getType()).isEqualTo(LIST);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Weger", "Marcel")).getType()).isEqualTo(DIRECT);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Winter", "Martin")).getType()).isEqualTo(NOT_ELECTED);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ziebarth", "Angelika")).getType()).isEqualTo(NOT_ELECTED);
		// @formatter:on

		softAssertions.assertAll();
	}

	/**
	 * Test votes using results of Klein Boden
	 */
	@Test
	@PackagePrivate
	void testVotesKleinBoden() {
		final LocalElectionResult resultRethwisch = PollingStationResultFilesTest.readResultsRethwisch();
		final LocalElection election = resultRethwisch.getElection();
		final LocalElectionResult result = resultRethwisch.filterByDistrict(
				LocalElectionTest.findPollingStation(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN));

		final SoftAssertions softAssertions = new SoftAssertions();
		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(softAssertions, result, true);

		// @formatter:off
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Beck", "Karsten")).getNumberOfVotes()).isEqualTo(57);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Behnk", "Sönke")).getNumberOfVotes()).isEqualTo(69);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Breede", "Rolf")).getNumberOfVotes()).isEqualTo(64);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Johannes")).getNumberOfVotes()).isEqualTo(16);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Volker")).getNumberOfVotes()).isEqualTo(26);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eggers", "Dirk")).getNumberOfVotes()).isEqualTo(71);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ehlert", "Armin")).getNumberOfVotes()).isEqualTo(12);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eick", "Ernst")).getNumberOfVotes()).isEqualTo(28);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Henning")).getNumberOfVotes()).isEqualTo(18);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Jan-Hendrik")).getNumberOfVotes()).isEqualTo(24);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Jögimar", "Helga")).getNumberOfVotes()).isEqualTo(10);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kraus", "Michael")).getNumberOfVotes()).isEqualTo(34);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kröger", "Dirk")).getNumberOfVotes()).isEqualTo(29);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "König", "Eva-Maria")).getNumberOfVotes()).isEqualTo(46);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kühn", "Steffen")).getNumberOfVotes()).isEqualTo(41);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Motzkus", "Dietrich")).getNumberOfVotes()).isEqualTo(57);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Poppinga", "Jens")).getNumberOfVotes()).isEqualTo(106);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Sauer", "Joachim")).getNumberOfVotes()).isEqualTo(11);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Schöning", "Mathias")).getNumberOfVotes()).isEqualTo(27);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Stapelfeldt", "Albert")).getNumberOfVotes()).isEqualTo(10);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Wahl", "Joachim")).getNumberOfVotes()).isEqualTo(37);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Weger", "Marcel")).getNumberOfVotes()).isEqualTo(57);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Winter", "Martin")).getNumberOfVotes()).isEqualTo(14);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ziebarth", "Angelika")).getNumberOfVotes()).isEqualTo(20);
		// @formatter:on

		softAssertions.assertAll();
	}

	/**
	 * Test votes using results of Rethwisch
	 */
	@Test
	@PackagePrivate
	void testVotesRethwisch() {
		final LocalElectionResult result = PollingStationResultFilesTest.readResultsRethwisch();
		final LocalElection election = result.getElection();

		final SoftAssertions softAssertions = new SoftAssertions();
		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(softAssertions, result, true);

		// @formatter:off
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Beck", "Karsten")).getNumberOfVotes()).isEqualTo(181);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Behnk", "Sönke")).getNumberOfVotes()).isEqualTo(220);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Breede", "Rolf")).getNumberOfVotes()).isEqualTo(146);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Johannes")).getNumberOfVotes()).isEqualTo(121);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Volker")).getNumberOfVotes()).isEqualTo(195);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eggers", "Dirk")).getNumberOfVotes()).isEqualTo(219);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ehlert", "Armin")).getNumberOfVotes()).isEqualTo(64);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eick", "Ernst")).getNumberOfVotes()).isEqualTo(112);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Henning")).getNumberOfVotes()).isEqualTo(151);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Jan-Hendrik")).getNumberOfVotes()).isEqualTo(160);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Jögimar", "Helga")).getNumberOfVotes()).isEqualTo(37);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kraus", "Michael")).getNumberOfVotes()).isEqualTo(116);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kröger", "Dirk")).getNumberOfVotes()).isEqualTo(75);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "König", "Eva-Maria")).getNumberOfVotes()).isEqualTo(115);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kühn", "Steffen")).getNumberOfVotes()).isEqualTo(150);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Motzkus", "Dietrich")).getNumberOfVotes()).isEqualTo(191);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Poppinga", "Jens")).getNumberOfVotes()).isEqualTo(328);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Sauer", "Joachim")).getNumberOfVotes()).isEqualTo(59);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Schöning", "Mathias")).getNumberOfVotes()).isEqualTo(85);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Stapelfeldt", "Albert")).getNumberOfVotes()).isEqualTo(73);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Wahl", "Joachim")).getNumberOfVotes()).isEqualTo(105);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Weger", "Marcel")).getNumberOfVotes()).isEqualTo(183);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Winter", "Martin")).getNumberOfVotes()).isEqualTo(87);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ziebarth", "Angelika")).getNumberOfVotes()).isEqualTo(53);
		// @formatter:on

		softAssertions.assertAll();
	}

	/**
	 * Test votes using results of Rethwischdorf
	 */
	@Test
	@PackagePrivate
	void testVotesRethwischdorf() {
		final LocalElectionResult resultRethwisch = PollingStationResultFilesTest.readResultsRethwisch();
		final LocalElection election = resultRethwisch.getElection();
		final LocalElectionResult result = resultRethwisch.filterByDistrict(
				LocalElectionTest.findPollingStation(election, LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF));

		final SoftAssertions softAssertions = new SoftAssertions();
		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(softAssertions, result, true);

		// @formatter:off
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Beck", "Karsten")).getNumberOfVotes()).isEqualTo(124);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Behnk", "Sönke")).getNumberOfVotes()).isEqualTo(151);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Breede", "Rolf")).getNumberOfVotes()).isEqualTo(82);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Johannes")).getNumberOfVotes()).isEqualTo(105);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Böttger", "Volker")).getNumberOfVotes()).isEqualTo(169);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eggers", "Dirk")).getNumberOfVotes()).isEqualTo(148);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ehlert", "Armin")).getNumberOfVotes()).isEqualTo(52);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Eick", "Ernst")).getNumberOfVotes()).isEqualTo(84);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Henning")).getNumberOfVotes()).isEqualTo(133);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Gäde", "Jan-Hendrik")).getNumberOfVotes()).isEqualTo(136);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Jögimar", "Helga")).getNumberOfVotes()).isEqualTo(27);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kraus", "Michael")).getNumberOfVotes()).isEqualTo(82);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kröger", "Dirk")).getNumberOfVotes()).isEqualTo(46);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "König", "Eva-Maria")).getNumberOfVotes()).isEqualTo(69);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Kühn", "Steffen")).getNumberOfVotes()).isEqualTo(109);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Motzkus", "Dietrich")).getNumberOfVotes()).isEqualTo(134);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Poppinga", "Jens")).getNumberOfVotes()).isEqualTo(222);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Sauer", "Joachim")).getNumberOfVotes()).isEqualTo(48);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Schöning", "Mathias")).getNumberOfVotes()).isEqualTo(58);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Stapelfeldt", "Albert")).getNumberOfVotes()).isEqualTo(63);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Wahl", "Joachim")).getNumberOfVotes()).isEqualTo(68);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Weger", "Marcel")).getNumberOfVotes()).isEqualTo(126);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Winter", "Martin")).getNumberOfVotes()).isEqualTo(73);
		softAssertions.assertThat(nominationResults.get(findNomination(election, "Ziebarth", "Angelika")).getNumberOfVotes()).isEqualTo(33);
		// @formatter:on

		softAssertions.assertAll();
	}
}
