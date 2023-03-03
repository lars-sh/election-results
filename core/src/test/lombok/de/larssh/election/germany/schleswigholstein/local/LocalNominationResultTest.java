package de.larssh.election.germany.schleswigholstein.local;

import static de.larssh.election.germany.schleswigholstein.local.LocalElectionTest.findNomination;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.DIRECT;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.DIRECT_BALANCE_SEAT;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.DIRECT_DRAW;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.DIRECT_DRAW_LIST;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.LIST;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.LIST_DRAW;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.LIST_OVERHANG_SEAT;
import static de.larssh.election.germany.schleswigholstein.local.LocalNominationResultType.NOT_ELECTED;
import static java.util.Arrays.asList;
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
	// TODO: Test with difference between result type and certain type
	/**
	 * Looks up a nomination result and asserts its number of votes, nomination
	 * result type and certain result type. This method is meant to be used for
	 * finished results. Therefore the expected certain result type is based on
	 * {@code expectedNominationResultType}.
	 *
	 * @param softAssertions               the {@link SoftAssertions} instance to
	 *                                     use for asserting
	 * @param result                       the election result to lookup the
	 *                                     nomination in
	 * @param familyName                   the nomination's family name
	 * @param givenName                    the nomination's given name
	 * @param expectedNumberOfVotes        the expected number of votes
	 * @param expectedNominationResultType the expected nomination result type
	 */
	private static void assertNomination(final SoftAssertions softAssertions,
			final LocalElectionResult result,
			final String familyName,
			final String givenName,
			final int expectedNumberOfVotes,
			final LocalNominationResultType expectedNominationResultType) {
		assertNomination(softAssertions,
				result,
				familyName,
				givenName,
				expectedNumberOfVotes,
				expectedNominationResultType,
				Optional.of(expectedNominationResultType));
	}

	/**
	 * Looks up a nomination result and asserts its number of votes, nomination
	 * result type and certain result type.
	 *
	 * @param softAssertions               the {@link SoftAssertions} instance to
	 *                                     use for asserting
	 * @param result                       the election result to lookup the
	 *                                     nomination in
	 * @param familyName                   the nomination's family name
	 * @param givenName                    the nomination's given name
	 * @param expectedNumberOfVotes        the expected number of votes
	 * @param expectedNominationResultType the expected nomination result type
	 * @param expectedCertainResultType    the expected certain result type
	 */
	private static void assertNomination(final SoftAssertions softAssertions,
			final LocalElectionResult result,
			final String familyName,
			final String givenName,
			final int expectedNumberOfVotes,
			final LocalNominationResultType expectedNominationResultType,
			final Optional<LocalNominationResultType> expectedCertainResultType) {
		assertNomination(softAssertions,
				result.getNominationResults().get(findNomination(result.getElection(), familyName, givenName)),
				expectedNumberOfVotes,
				expectedNominationResultType,
				expectedCertainResultType);
	}

	/**
	 * Asserts the nomination result's number of votes, nomination result type and
	 * certain result type.
	 *
	 * @param softAssertions               the {@link SoftAssertions} instance to
	 *                                     use for asserting
	 * @param nominationResult             the nomination result to assert
	 * @param expectedNumberOfVotes        the expected number of votes
	 * @param expectedNominationResultType the expected nomination result type
	 * @param expectedCertainResultType    the expected certain result type
	 */
	private static void assertNomination(final SoftAssertions softAssertions,
			final LocalNominationResult nominationResult,
			final int expectedNumberOfVotes,
			final LocalNominationResultType expectedNominationResultType,
			final Optional<LocalNominationResultType> expectedCertainResultType) {
		softAssertions.assertThat(nominationResult.getNumberOfVotes())
				.describedAs("Number of Votes of \"%s\"", nominationResult.getNomination().getKey())
				.isEqualTo(expectedNumberOfVotes);
		softAssertions.assertThat(nominationResult.getType())
				.describedAs("Nomination Result Type \"%s\"", nominationResult.getNomination().getKey())
				.isEqualTo(expectedNominationResultType);
		softAssertions.assertThat(nominationResult.getCertainResultType())
				.describedAs("Certain Result Type of \"%s\"", nominationResult.getNomination().getKey())
				.isEqualTo(expectedCertainResultType);
	}

	/**
	 * Asserts the nomination results in the case specified by the input data read
	 * using {@code testKind} evaluated against the supplied expected values.
	 *
	 * @param testKind                     the test kind, used for the file name
	 * @param expectedNominationResultType a function taking the currently tested
	 *                                     nomination result and returning the
	 *                                     expected nomination result type
	 * @param expectedCertainResultType    a function taking the currently tested
	 *                                     nomination result and returning the
	 *                                     expected certain result type
	 */
	private static void assertResultTypesForAllNominations(final String testKind,
			final int expectedNumberOfVotes,
			final Function<LocalNominationResult, LocalNominationResultType> expectedNominationResultType,
			final Function<LocalNominationResult, Optional<LocalNominationResultType>> expectedCertainResultType) {
		final LocalElectionResult result = readResultKleinBoden(LocalElectionTest.createElection(),
				Paths.get("../LocalNominationResult-" + testKind + ".txt"));

		final SoftAssertions softAssertions = new SoftAssertions();
		for (final LocalNominationResult nominationResult : result.getNominationResults().values()) {
			assertNomination(softAssertions,
					nominationResult,
					nominationResult.getNomination().isDirectNomination() ? expectedNumberOfVotes : 0,
					expectedNominationResultType.apply(nominationResult),
					expectedCertainResultType.apply(nominationResult));
		}
		softAssertions.assertAll();
	}

	/**
	 * TODO
	 *
	 * @param nominationResult
	 * @return
	 */
	private static LocalNominationResultType getExpectedAllOneNominationResultType(
			final LocalNominationResult nominationResult) {
		if (!nominationResult.getNomination().isDirectNomination()) {
			return NOT_ELECTED;
		}
		return nominationResult.getNomination().getListPosition().orElse(Integer.MAX_VALUE) < 4
				// TODO: &&
				// !nominationResult.getNomination().getPerson().getFamilyName().equals("Kraus")
				? DIRECT_DRAW_LIST
				: DIRECT_DRAW;
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
	 * Tests the Sainte Laguë values of the local district Rethwisch based on the
	 * official results from 2018
	 */
	@Test
	@PackagePrivate
	void testSainteLagueValuesRethwisch() {
		final LocalElectionResult result = PollingStationResultFilesTest.readResultsRethwisch();
		final LocalElection election = result.getElection();

		final SoftAssertions softAssertions = new SoftAssertions();
		final Map<LocalNomination, LocalNominationResult> nominationResults = result.getNominationResults();

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
	 * Tests nomination results in case no ballot was evaluated and the election
	 * evaluation is not started.
	 */
	@Test
	@PackagePrivate
	void testAllZeroNotStarted() {
		assertResultTypesForAllNominations("all-zero-not-started",
				0,
				nominationResult -> NOT_ELECTED,
				nominationResult -> Optional.empty());
	}

	/**
	 * Tests nomination results in case no ballot was evaluated and the election
	 * evaluation is partially done.
	 */
	@Test
	@PackagePrivate
	void testAllZeroPartiallyDone() {
		assertResultTypesForAllNominations("all-zero-partially-done",
				0,
				nominationResult -> NOT_ELECTED,
				nominationResult -> Optional.empty());
	}

	/**
	 * Tests nomination results in case no ballot was evaluated and the election
	 * evaluation finished.
	 */
	@Test
	@PackagePrivate
	void testAllZeroFinished() {
		assertResultTypesForAllNominations("all-zero-finished",
				0,
				nominationResult -> NOT_ELECTED,
				nominationResult -> Optional.of(NOT_ELECTED));
	}

	/**
	 * Tests nomination results in case exactly the same number of votes were given
	 * to all nominations and the election evaluation is not started.
	 */
	@Test
	@PackagePrivate
	void testAllOneNotStarted() {
		assertResultTypesForAllNominations("all-one-not-started",
				1,
				LocalNominationResultTest::getExpectedAllOneNominationResultType,
				nominationResult -> Optional.empty());
	}

	/**
	 * Tests nomination results in case exactly the same number of votes were given
	 * to all nominations and the election evaluation is partially done.
	 */
	@Test
	@PackagePrivate
	void testAllOnePartiallyDone() {
		assertResultTypesForAllNominations("all-one-partially-done",
				1,
				LocalNominationResultTest::getExpectedAllOneNominationResultType,
				nominationResult -> Optional.empty());
	}

	/**
	 * Tests nomination results in case exactly the same number of votes were given
	 * to all nominations and the election evaluation is finished.
	 */
	@Test
	@PackagePrivate
	void testAllOneFinished() {
		assertResultTypesForAllNominations("all-one-finished",
				1,
				LocalNominationResultTest::getExpectedAllOneNominationResultType,
				nominationResult -> Optional.of(nominationResult.getType()));
	}

	/**
	 * Test types in case of balance and overhang seats
	 */
	@Test
	@PackagePrivate
	void testBalanceAndOverhangSeats() {
		final LocalElectionResult result = readResultKleinBoden(LocalElectionTest.createElection(),
				Paths.get("../LocalNominationResult-balance-and-overhang-seats.txt"));

		final SoftAssertions softAssertions = new SoftAssertions();
		assertNomination(softAssertions, result, "Beck", "Karsten", 0, LIST_OVERHANG_SEAT);
		assertNomination(softAssertions, result, "Behnk", "Sönke", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Bernhardt", "Christian", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Breede", "Rolf", 12, NOT_ELECTED);
		assertNomination(softAssertions, result, "Böttger", "Johannes", 20, LIST);
		assertNomination(softAssertions, result, "Böttger", "Volker", 19, LIST_OVERHANG_SEAT);
		assertNomination(softAssertions, result, "Dohrendorf", "Martina", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Dohrendorf", "Thomas", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Efrom", "Joachim", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Eggers", "Dirk", 0, LIST);
		assertNomination(softAssertions, result, "Ehlert", "Armin", 24, DIRECT);
		assertNomination(softAssertions, result, "Eick", "Ernst", 26, DIRECT);
		assertNomination(softAssertions, result, "Feddern", "Axel", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Feddern", "Hartmut", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Gräpel", "Carola", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Gäde", "Henning", 17, NOT_ELECTED);
		assertNomination(softAssertions, result, "Gäde", "Jan-Hendrik", 21, LIST);
		assertNomination(softAssertions, result, "Hartz", "Catrin", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Jögimar", "Helga", 25, DIRECT);
		assertNomination(softAssertions, result, "Klein", "Erik", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Kraus", "Michael", 13, NOT_ELECTED);
		assertNomination(softAssertions, result, "Kröger", "Dirk", 27, DIRECT);
		assertNomination(softAssertions, result, "König", "Eva-Maria", 10, NOT_ELECTED);
		assertNomination(softAssertions, result, "Kühn", "Steffen", 15, LIST);
		assertNomination(softAssertions, result, "Motzkus", "Dietrich", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Poppinga", "Jens", 100, DIRECT);
		assertNomination(softAssertions, result, "Sauer", "Joachim", 22, NOT_ELECTED);
		assertNomination(softAssertions, result, "Schwarz", "Rupert", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Schöning", "Mathias", 11, NOT_ELECTED);
		assertNomination(softAssertions, result, "Stapelfeldt", "Albert", 16, NOT_ELECTED);
		assertNomination(softAssertions, result, "Topel", "Andreas", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Wahl", "Joachim", 14, LIST);
		assertNomination(softAssertions, result, "Weger", "Marcel", 0, NOT_ELECTED);
		assertNomination(softAssertions, result, "Winter", "Martin", 18, NOT_ELECTED);
		assertNomination(softAssertions,
				result,
				"Ziebarth",
				"Angelika",
				23,
				DIRECT_BALANCE_SEAT,
				Optional.of(DIRECT_BALANCE_SEAT));
		softAssertions.assertAll();
	}

	/**
	 * Test types in case of completely open draws
	 */
	@Test
	@PackagePrivate
	void testOpenDraws() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result
				= readResultKleinBoden(election, Paths.get("../LocalNominationResult-draws.txt"));

		final SoftAssertions softAssertions = new SoftAssertions();
		assertNomination(softAssertions, result, "Beck", "Karsten", 4, LIST_DRAW);
		assertNomination(softAssertions, result, "Behnk", "Sönke", 2, NOT_ELECTED);
		assertNomination(softAssertions, result, "Breede", "Rolf", 3, NOT_ELECTED);
		assertNomination(softAssertions, result, "Böttger", "Johannes", 5, DIRECT_DRAW); // TODO
		assertNomination(softAssertions, result, "Böttger", "Volker", 4, LIST_DRAW);
		assertNomination(softAssertions, result, "Eggers", "Dirk", 5, DIRECT_DRAW); // TODO
		assertNomination(softAssertions, result, "Ehlert", "Armin", 3, NOT_ELECTED);
		assertNomination(softAssertions, result, "Eick", "Ernst", 5, DIRECT_DRAW); // TODO
		assertNomination(softAssertions, result, "Gäde", "Henning", 2, NOT_ELECTED);
		assertNomination(softAssertions, result, "Gäde", "Jan-Hendrik", 6, DIRECT);
		assertNomination(softAssertions, result, "Jögimar", "Helga", 4, LIST_DRAW);
		assertNomination(softAssertions, result, "Kraus", "Michael", 4, LIST_DRAW);
		assertNomination(softAssertions, result, "Kröger", "Dirk", 6, DIRECT);
		assertNomination(softAssertions, result, "König", "Eva-Maria", 1, NOT_ELECTED);
		assertNomination(softAssertions, result, "Kühn", "Steffen", 6, DIRECT);
		assertNomination(softAssertions, result, "Motzkus", "Dietrich", 3, NOT_ELECTED);
		assertNomination(softAssertions, result, "Poppinga", "Jens", 6, DIRECT);
		assertNomination(softAssertions, result, "Sauer", "Joachim", 1, NOT_ELECTED);
		assertNomination(softAssertions, result, "Schöning", "Mathias", 2, NOT_ELECTED);
		assertNomination(softAssertions, result, "Stapelfeldt", "Albert", 1, NOT_ELECTED);
		assertNomination(softAssertions, result, "Wahl", "Joachim", 5, DIRECT_DRAW); // TODO
		assertNomination(softAssertions, result, "Weger", "Marcel", 1, NOT_ELECTED);
		assertNomination(softAssertions, result, "Winter", "Martin", 3, NOT_ELECTED);
		assertNomination(softAssertions, result, "Ziebarth", "Angelika", 2, NOT_ELECTED);
		softAssertions.assertAll();
	}

	/**
	 * Test types in case of partially closed draws in case there are less list draw
	 * results than list positions
	 */
	@Test
	@PackagePrivate
	void testPartiallyClosedDraws1() { // TODO: Finish
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult resultWithoutDrawResults
				= readResultKleinBoden(election, Paths.get("../LocalNominationResult-draws.txt"));
		final LocalElectionResult result = new LocalElectionResult(election,
				2,
				resultWithoutDrawResults.getNumberOfAllBallotsMap(),
				new HashSet<>(asList(findNomination(election, "Böttger", "Johannes"),
						findNomination(election, "Eggers", "Dirk"))),
				singleton(findNomination(election, "Beck", "Karsten")),
				resultWithoutDrawResults.getBallots());

		final SoftAssertions softAssertions = new SoftAssertions();
		assertNomination(softAssertions, result, "Beck", "Karsten", 4, LIST);
		assertNomination(softAssertions, result, "Behnk", "Sönke", 2, NOT_ELECTED);
		assertNomination(softAssertions, result, "Breede", "Rolf", 3, NOT_ELECTED);
		assertNomination(softAssertions, result, "Böttger", "Johannes", 5, DIRECT);
		assertNomination(softAssertions, result, "Böttger", "Volker", 4, LIST_DRAW);
		assertNomination(softAssertions, result, "Eggers", "Dirk", 5, DIRECT);
		assertNomination(softAssertions, result, "Ehlert", "Armin", 3, NOT_ELECTED);
		assertNomination(softAssertions, result, "Eick", "Ernst", 5, LIST);
		assertNomination(softAssertions, result, "Gäde", "Henning", 2, NOT_ELECTED);
		assertNomination(softAssertions, result, "Gäde", "Jan-Hendrik", 6, DIRECT);
		assertNomination(softAssertions, result, "Jögimar", "Helga", 4, LIST_DRAW);
		assertNomination(softAssertions, result, "Kraus", "Michael", 4, LIST_DRAW);
		assertNomination(softAssertions, result, "Kröger", "Dirk", 6, DIRECT);
		assertNomination(softAssertions, result, "König", "Eva-Maria", 1, NOT_ELECTED);
		assertNomination(softAssertions, result, "Kühn", "Steffen", 6, DIRECT);
		assertNomination(softAssertions, result, "Motzkus", "Dietrich", 3, NOT_ELECTED);
		assertNomination(softAssertions, result, "Poppinga", "Jens", 6, DIRECT);
		assertNomination(softAssertions, result, "Sauer", "Joachim", 1, NOT_ELECTED);
		assertNomination(softAssertions, result, "Schöning", "Mathias", 2, NOT_ELECTED);
		assertNomination(softAssertions, result, "Stapelfeldt", "Albert", 1, NOT_ELECTED);
		assertNomination(softAssertions, result, "Wahl", "Joachim", 5, LIST);
		assertNomination(softAssertions, result, "Weger", "Marcel", 1, NOT_ELECTED);
		assertNomination(softAssertions, result, "Winter", "Martin", 3, NOT_ELECTED);
		assertNomination(softAssertions, result, "Ziebarth", "Angelika", 2, NOT_ELECTED);
		softAssertions.assertAll();
	}

	/**
	 * Test types in case of partially closed draws in case there are less direct
	 * draw results than direct positions
	 */
	@Test
	@PackagePrivate
	void testPartiallyClosedDraws2() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult resultWithoutDrawResults
				= readResultKleinBoden(election, Paths.get("../LocalNominationResult-draws.txt"));
		final LocalElectionResult result = new LocalElectionResult(election,
				2,
				resultWithoutDrawResults.getNumberOfAllBallotsMap(),
				singleton(findNomination(election, "Böttger", "Johannes")),
				new HashSet<>(asList(findNomination(election, "Beck", "Karsten"),
						findNomination(election, "Böttger", "Volker"),
						findNomination(election, "Jögimar", "Helga"))),
				resultWithoutDrawResults.getBallots());

		final SoftAssertions softAssertions = new SoftAssertions();
		assertNomination(softAssertions, result, "Beck", "Karsten", 4, LIST);
		assertNomination(softAssertions, result, "Behnk", "Sönke", 2, NOT_ELECTED);
		assertNomination(softAssertions, result, "Breede", "Rolf", 3, NOT_ELECTED);
		assertNomination(softAssertions, result, "Böttger", "Johannes", 5, DIRECT);
		assertNomination(softAssertions, result, "Böttger", "Volker", 4, LIST);
		assertNomination(softAssertions, result, "Eggers", "Dirk", 5, DIRECT_DRAW_LIST);
		assertNomination(softAssertions, result, "Ehlert", "Armin", 3, NOT_ELECTED);
		assertNomination(softAssertions, result, "Eick", "Ernst", 5, DIRECT_DRAW_LIST);
		assertNomination(softAssertions, result, "Gäde", "Henning", 2, NOT_ELECTED);
		assertNomination(softAssertions, result, "Gäde", "Jan-Hendrik", 6, DIRECT);
		assertNomination(softAssertions, result, "Jögimar", "Helga", 4, LIST);
		assertNomination(softAssertions, result, "Kraus", "Michael", 4, NOT_ELECTED);
		assertNomination(softAssertions, result, "Kröger", "Dirk", 6, DIRECT);
		assertNomination(softAssertions, result, "König", "Eva-Maria", 1, NOT_ELECTED);
		assertNomination(softAssertions, result, "Kühn", "Steffen", 6, DIRECT);
		assertNomination(softAssertions, result, "Motzkus", "Dietrich", 3, NOT_ELECTED);
		assertNomination(softAssertions, result, "Poppinga", "Jens", 6, DIRECT);
		assertNomination(softAssertions, result, "Sauer", "Joachim", 1, NOT_ELECTED);
		assertNomination(softAssertions, result, "Schöning", "Mathias", 2, NOT_ELECTED);
		assertNomination(softAssertions, result, "Stapelfeldt", "Albert", 1, NOT_ELECTED);
		assertNomination(softAssertions, result, "Wahl", "Joachim", 5, DIRECT_DRAW); // TODO
		assertNomination(softAssertions, result, "Weger", "Marcel", 1, NOT_ELECTED);
		assertNomination(softAssertions, result, "Winter", "Martin", 3, NOT_ELECTED);
		assertNomination(softAssertions, result, "Ziebarth", "Angelika", 2, NOT_ELECTED);
		softAssertions.assertAll();
	}

	/**
	 * Tests nomination results using results of Klein Boden
	 */
	@Test
	@PackagePrivate
	void testKleinBoden() {
		final LocalElectionResult resultRethwisch = PollingStationResultFilesTest.readResultsRethwisch();
		final LocalElectionResult result = resultRethwisch.filterByDistrict(LocalElectionTest
				.findPollingStation(resultRethwisch.getElection(), LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN));

		final SoftAssertions softAssertions = new SoftAssertions();
		assertNomination(softAssertions, result, "Beck", "Karsten", 57, DIRECT_DRAW_LIST, Optional.empty());
		assertNomination(softAssertions, result, "Behnk", "Sönke", 69, DIRECT, Optional.empty());
		assertNomination(softAssertions, result, "Breede", "Rolf", 64, DIRECT, Optional.empty());
		assertNomination(softAssertions, result, "Böttger", "Johannes", 16, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Böttger", "Volker", 26, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Eggers", "Dirk", 71, DIRECT, Optional.empty());
		assertNomination(softAssertions, result, "Ehlert", "Armin", 12, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Eick", "Ernst", 28, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Gäde", "Henning", 18, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Gäde", "Jan-Hendrik", 24, LIST, Optional.empty());
		assertNomination(softAssertions, result, "Jögimar", "Helga", 10, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Kraus", "Michael", 34, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Kröger", "Dirk", 29, LIST, Optional.empty());
		assertNomination(softAssertions, result, "König", "Eva-Maria", 46, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Kühn", "Steffen", 41, LIST, Optional.empty());
		assertNomination(softAssertions, result, "Motzkus", "Dietrich", 57, DIRECT_DRAW_LIST, Optional.empty());
		assertNomination(softAssertions, result, "Poppinga", "Jens", 106, DIRECT, Optional.empty());
		assertNomination(softAssertions, result, "Sauer", "Joachim", 11, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Schöning", "Mathias", 27, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Stapelfeldt", "Albert", 10, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Wahl", "Joachim", 37, LIST, Optional.empty());
		assertNomination(softAssertions, result, "Weger", "Marcel", 57, DIRECT_DRAW_LIST, Optional.empty());
		assertNomination(softAssertions, result, "Winter", "Martin", 14, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Ziebarth", "Angelika", 20, NOT_ELECTED, Optional.empty());
		softAssertions.assertAll();
	}

	/**
	 * Tests nomination results using results of Rethwisch
	 */
	@Test
	@PackagePrivate
	void testRethwisch() {
		final LocalElectionResult result = PollingStationResultFilesTest.readResultsRethwisch();

		final SoftAssertions softAssertions = new SoftAssertions();
		assertNomination(softAssertions, result, "Beck", "Karsten", 181, NOT_ELECTED);
		assertNomination(softAssertions, result, "Behnk", "Sönke", 220, DIRECT);
		assertNomination(softAssertions, result, "Breede", "Rolf", 146, NOT_ELECTED);
		assertNomination(softAssertions, result, "Böttger", "Johannes", 121, LIST);
		assertNomination(softAssertions, result, "Böttger", "Volker", 195, DIRECT);
		assertNomination(softAssertions, result, "Eggers", "Dirk", 219, DIRECT);
		assertNomination(softAssertions, result, "Ehlert", "Armin", 64, NOT_ELECTED);
		assertNomination(softAssertions, result, "Eick", "Ernst", 112, NOT_ELECTED);
		assertNomination(softAssertions, result, "Gäde", "Henning", 151, NOT_ELECTED);
		assertNomination(softAssertions, result, "Gäde", "Jan-Hendrik", 160, LIST);
		assertNomination(softAssertions, result, "Jögimar", "Helga", 37, NOT_ELECTED);
		assertNomination(softAssertions, result, "Kraus", "Michael", 116, NOT_ELECTED);
		assertNomination(softAssertions, result, "Kröger", "Dirk", 75, LIST);
		assertNomination(softAssertions, result, "König", "Eva-Maria", 115, NOT_ELECTED);
		assertNomination(softAssertions, result, "Kühn", "Steffen", 150, LIST);
		assertNomination(softAssertions, result, "Motzkus", "Dietrich", 191, DIRECT);
		assertNomination(softAssertions, result, "Poppinga", "Jens", 328, DIRECT);
		assertNomination(softAssertions, result, "Sauer", "Joachim", 59, NOT_ELECTED);
		assertNomination(softAssertions, result, "Schöning", "Mathias", 85, NOT_ELECTED);
		assertNomination(softAssertions, result, "Stapelfeldt", "Albert", 73, NOT_ELECTED);
		assertNomination(softAssertions, result, "Wahl", "Joachim", 105, LIST);
		assertNomination(softAssertions, result, "Weger", "Marcel", 183, DIRECT);
		assertNomination(softAssertions, result, "Winter", "Martin", 87, NOT_ELECTED);
		assertNomination(softAssertions, result, "Ziebarth", "Angelika", 53, NOT_ELECTED);
		softAssertions.assertAll();
	}

	/**
	 * Tests nomination results using results of Rethwischdorf
	 */
	@Test
	@PackagePrivate
	void testRethwischdorf() {
		final LocalElectionResult resultRethwisch = PollingStationResultFilesTest.readResultsRethwisch();
		final LocalElectionResult result
				= resultRethwisch.filterByDistrict(LocalElectionTest.findPollingStation(resultRethwisch.getElection(),
						LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF));

		final SoftAssertions softAssertions = new SoftAssertions();
		assertNomination(softAssertions, result, "Beck", "Karsten", 124, LIST, Optional.empty());
		assertNomination(softAssertions, result, "Behnk", "Sönke", 151, DIRECT, Optional.empty());
		assertNomination(softAssertions, result, "Breede", "Rolf", 82, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Böttger", "Johannes", 105, LIST, Optional.empty());
		assertNomination(softAssertions, result, "Böttger", "Volker", 169, DIRECT, Optional.empty());
		assertNomination(softAssertions, result, "Eggers", "Dirk", 148, DIRECT, Optional.empty());
		assertNomination(softAssertions, result, "Ehlert", "Armin", 52, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Eick", "Ernst", 84, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Gäde", "Henning", 133, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Gäde", "Jan-Hendrik", 136, DIRECT, Optional.empty());
		assertNomination(softAssertions, result, "Jögimar", "Helga", 27, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Kraus", "Michael", 82, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Kröger", "Dirk", 46, LIST, Optional.empty());
		assertNomination(softAssertions, result, "König", "Eva-Maria", 69, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Kühn", "Steffen", 109, LIST, Optional.empty());
		assertNomination(softAssertions, result, "Motzkus", "Dietrich", 134, DIRECT, Optional.empty());
		assertNomination(softAssertions, result, "Poppinga", "Jens", 222, DIRECT, Optional.empty());
		assertNomination(softAssertions, result, "Sauer", "Joachim", 48, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Schöning", "Mathias", 58, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Stapelfeldt", "Albert", 63, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Wahl", "Joachim", 68, LIST, Optional.empty());
		assertNomination(softAssertions, result, "Weger", "Marcel", 126, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Winter", "Martin", 73, NOT_ELECTED, Optional.empty());
		assertNomination(softAssertions, result, "Ziebarth", "Angelika", 33, NOT_ELECTED, Optional.empty());
		softAssertions.assertAll();
	}
}
