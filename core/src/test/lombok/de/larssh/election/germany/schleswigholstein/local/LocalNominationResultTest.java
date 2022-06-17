package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.larssh.election.germany.schleswigholstein.local.legacy.LegacyParserTest;
import de.larssh.utils.annotations.PackagePrivate;
import lombok.NoArgsConstructor;

/**
 * {@link LegacyParser}
 */
@PackagePrivate
@NoArgsConstructor
class LocalNominationResultTest {
	/**
	 * Executes {@link LocalElectionResult#getNominationResults()} and asserts the
	 * result for the expected number of results.
	 *
	 * <p>
	 * {@code assertListNominationsAsNotElected} can be specified to test if list
	 * nominations are not elected, which is the case as long as no balance seat is
	 * involved.
	 *
	 * @param result                            the {@link LocalElectionResult}
	 * @param assertListNominationsAsNotElected {@code true} if list nominations
	 *                                          shall be asserted as not elected,
	 *                                          else {@code false}
	 * @return Wahlergebnis einzelner Bewerberinnen und Bewerber
	 */
	private static Map<LocalNomination, LocalNominationResult> getAndAssertNominationResults(
			final LocalElectionResult result,
			final boolean assertListNominationsAsNotElected) {
		final Map<LocalNomination, LocalNominationResult> nominationResults = result.getNominationResults();

		assertThat(nominationResults).hasSameSizeAs(result.getElection().getNominations());
		if (assertListNominationsAsNotElected) {
			result.getNominationResults()
					.values()
					.stream()
					.filter(nominationResult -> nominationResult.getNomination().getType() == LocalNominationType.LIST)
					.forEach(nominationResult -> assertThat(nominationResult.getType())
							.isEqualTo(LocalNominationResultType.NOT_ELECTED));
		}

		return nominationResults;
	}

	/**
	 * Test the Sainte Laguë value of the local district Rethwisch based on the
	 * official results from 2018
	 */
	@Test
	@PackagePrivate
	void testSainteLagueRethwisch() {
		final LocalElectionResult result = LegacyParserTest.readResultsRethwisch();
		final LocalElection election = result.getElection();

		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(result, true);

		// @formatter:off
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(24036, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(88133, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Bernhardt", "Christian")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(12590, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(20486, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(31480, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(157400, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Dohrendorf", "Martina")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(13916, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Dohrendorf", "Thomas")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(9560, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Efrom", "Joachim")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(8435, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(52880, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(11429, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(26667, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Feddern", "Axel")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(7547, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Feddern", "Hartmut")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(6829, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gräpel", "Carola")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(20338, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(17489, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(52467, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Hartz", "Catrin")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(11031, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(16000, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Klein", "Erik")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(15553, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(28680, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(80000, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(13036, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(143400, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(37771, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(264400, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(7273, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schwarz", "Rupert")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(17627, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(15933, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(14309, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Topel", "Andreas")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(11496, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(47800, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(29378, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(22486, 2));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(8889, 2));
		// @formatter:on
	}

	/**
	 * Test types in case no ballot was evaluated
	 */
	@Test
	@PackagePrivate
	void testTypeAllZero() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result = LegacyParserTest.readResult(election,
				LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN,
				Paths.get("LocalNominationResult-all-zero.txt"));

		getAndAssertNominationResults(result, true).values()
				.forEach(nominationResult -> assertThat(nominationResult.getType())
						.isEqualTo(LocalNominationResultType.NOT_ELECTED));
	}

	/**
	 * Test types in case exactly the same number of votes were given to all
	 * nominations.
	 */
	@Test
	@PackagePrivate
	void testTypeAllOne() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result = LegacyParserTest.readResult(election,
				LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN,
				Paths.get("LocalNominationResult-all-one.txt"));

		getAndAssertNominationResults(result, true).values()
				.stream()
				.filter(nominationResult -> nominationResult.getNomination().getType() == LocalNominationType.DIRECT)
				.forEach(nominationResult -> assertThat(nominationResult.getType())
						.isEqualTo(LocalNominationResultType.DIRECT_DRAW));
	}

	/**
	 * Test types in case of balance and overhang seats
	 */
	@Test
	@PackagePrivate
	void testTypeBalanceAndOverhangSeats() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result = LegacyParserTest.readResult(election,
				LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN,
				Paths.get("LocalNominationResult-balance-and-overhang-seats.txt"));

		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(result, false);

		// @formatter:off
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getType()).isEqualTo(LocalNominationResultType.LIST_OVERHANG_SEAT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Bernhardt", "Christian")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Dohrendorf", "Martina")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Dohrendorf", "Thomas")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Efrom", "Joachim")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Feddern", "Axel")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Feddern", "Hartmut")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gräpel", "Carola")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Hartz", "Catrin")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Klein", "Erik")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schwarz", "Rupert")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Topel", "Andreas")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getType()).isEqualTo(LocalNominationResultType.DIRECT_BALANCE_SEAT);
		// @formatter:on
	}

	/**
	 * Test types in case of partially closed draws in case there are less list draw
	 * results than list positions
	 */
	@Test
	@PackagePrivate
	void testTypePartiallyClosedDraws1() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult resultWithoutDrawResults = LegacyParserTest.readResult(election,
				LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN,
				Paths.get("LocalNominationResult-draws.txt"));
		final LocalElectionResult result = new LocalElectionResult(election,
				resultWithoutDrawResults.getNumberOfAllBallots(),
				resultWithoutDrawResults.getBallots(),
				new HashSet<>(asList(LocalElectionTest.findNomination(election, "Böttger", "Johannes"),
						LocalElectionTest.findNomination(election, "Eggers", "Dirk"))),
				singleton(LocalElectionTest.findNomination(election, "Beck", "Karsten")));

		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(result, true);

		// @formatter:off
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getType()).isEqualTo(LocalNominationResultType.LIST_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getType()).isEqualTo(LocalNominationResultType.LIST_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getType()).isEqualTo(LocalNominationResultType.LIST_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		// @formatter:on
	}

	/**
	 * Test types in case of partially closed draws in case there are less direct
	 * draw results than direct positions
	 */
	@Test
	@PackagePrivate
	void testTypePartiallyClosedDraws2() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult resultWithoutDrawResults = LegacyParserTest.readResult(election,
				LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN,
				Paths.get("LocalNominationResult-draws.txt"));
		final LocalElectionResult result = new LocalElectionResult(election,
				resultWithoutDrawResults.getNumberOfAllBallots(),
				resultWithoutDrawResults.getBallots(),
				singleton(LocalElectionTest.findNomination(election, "Böttger", "Johannes")),
				new HashSet<>(asList(LocalElectionTest.findNomination(election, "Beck", "Karsten"),
						LocalElectionTest.findNomination(election, "Böttger", "Volker"),
						LocalElectionTest.findNomination(election, "Jögimar", "Helga"))));

		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(result, true);

		// @formatter:off
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getType()).isEqualTo(LocalNominationResultType.DIRECT_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getType()).isEqualTo(LocalNominationResultType.DIRECT_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getType()).isEqualTo(LocalNominationResultType.DIRECT_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		// @formatter:on
	}

	/**
	 * Test types in case of completely open draws
	 */
	@Test
	@PackagePrivate
	void testTypeOpenDraws() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result = LegacyParserTest.readResult(election,
				LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN,
				Paths.get("LocalNominationResult-draws.txt"));

		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(result, true);

		// @formatter:off
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getType()).isEqualTo(LocalNominationResultType.LIST_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getType()).isEqualTo(LocalNominationResultType.DIRECT_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getType()).isEqualTo(LocalNominationResultType.LIST_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getType()).isEqualTo(LocalNominationResultType.DIRECT_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getType()).isEqualTo(LocalNominationResultType.DIRECT_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getType()).isEqualTo(LocalNominationResultType.LIST_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getType()).isEqualTo(LocalNominationResultType.LIST_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getType()).isEqualTo(LocalNominationResultType.DIRECT_DRAW);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		// @formatter:on
	}

	/**
	 * Test types using results of Rethwisch
	 */
	@Test
	@PackagePrivate
	void testTypeRethwisch() {
		final LocalElectionResult result = LegacyParserTest.readResultsRethwisch();
		final LocalElection election = result.getElection();

		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(result, true);

		// @formatter:off
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Bernhardt", "Christian")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Dohrendorf", "Martina")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Dohrendorf", "Thomas")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Efrom", "Joachim")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Feddern", "Axel")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Feddern", "Hartmut")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gräpel", "Carola")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Hartz", "Catrin")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Klein", "Erik")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schwarz", "Rupert")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Topel", "Andreas")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getType()).isEqualTo(LocalNominationResultType.LIST);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getType()).isEqualTo(LocalNominationResultType.DIRECT);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getType()).isEqualTo(LocalNominationResultType.NOT_ELECTED);
		// @formatter:on
	}

	/**
	 * Test votes using results of Klein Boden
	 */
	@Test
	@PackagePrivate
	void testVotesKleinBoden() {
		final LocalElectionResult result = LegacyParserTest.readResultsRethwisch()
				.filter(ballot -> ballot.getPollingStation()
						.getName()
						.equals(LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN));
		final LocalElection election = result.getElection();

		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(result, true);

		// @formatter:off
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getNumberOfVotes()).isEqualTo(57);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getNumberOfVotes()).isEqualTo(69);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getNumberOfVotes()).isEqualTo(64);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getNumberOfVotes()).isEqualTo(16);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getNumberOfVotes()).isEqualTo(26);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getNumberOfVotes()).isEqualTo(71);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getNumberOfVotes()).isEqualTo(12);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getNumberOfVotes()).isEqualTo(28);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getNumberOfVotes()).isEqualTo(18);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getNumberOfVotes()).isEqualTo(24);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getNumberOfVotes()).isEqualTo(10);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getNumberOfVotes()).isEqualTo(34);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getNumberOfVotes()).isEqualTo(29);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getNumberOfVotes()).isEqualTo(46);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getNumberOfVotes()).isEqualTo(41);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getNumberOfVotes()).isEqualTo(57);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getNumberOfVotes()).isEqualTo(106);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getNumberOfVotes()).isEqualTo(11);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getNumberOfVotes()).isEqualTo(27);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getNumberOfVotes()).isEqualTo(10);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getNumberOfVotes()).isEqualTo(37);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getNumberOfVotes()).isEqualTo(57);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getNumberOfVotes()).isEqualTo(14);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getNumberOfVotes()).isEqualTo(20);
		// @formatter:on
	}

	/**
	 * Test votes using results of Rethwisch
	 */
	@Test
	@PackagePrivate
	void testVotesRethwisch() {
		final LocalElectionResult result = LegacyParserTest.readResultsRethwisch();
		final LocalElection election = result.getElection();

		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(result, true);

		// @formatter:off
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getNumberOfVotes()).isEqualTo(181);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getNumberOfVotes()).isEqualTo(220);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getNumberOfVotes()).isEqualTo(146);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getNumberOfVotes()).isEqualTo(121);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getNumberOfVotes()).isEqualTo(195);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getNumberOfVotes()).isEqualTo(219);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getNumberOfVotes()).isEqualTo(64);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getNumberOfVotes()).isEqualTo(112);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getNumberOfVotes()).isEqualTo(151);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getNumberOfVotes()).isEqualTo(160);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getNumberOfVotes()).isEqualTo(37);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getNumberOfVotes()).isEqualTo(116);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getNumberOfVotes()).isEqualTo(75);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getNumberOfVotes()).isEqualTo(115);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getNumberOfVotes()).isEqualTo(150);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getNumberOfVotes()).isEqualTo(191);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getNumberOfVotes()).isEqualTo(328);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getNumberOfVotes()).isEqualTo(59);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getNumberOfVotes()).isEqualTo(85);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getNumberOfVotes()).isEqualTo(73);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getNumberOfVotes()).isEqualTo(105);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getNumberOfVotes()).isEqualTo(183);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getNumberOfVotes()).isEqualTo(87);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getNumberOfVotes()).isEqualTo(53);
		// @formatter:on
	}

	/**
	 * Test votes using results of Rethwischdorf
	 */
	@Test
	@PackagePrivate
	void testVotesRethwischdorf() {
		final LocalElectionResult result = LegacyParserTest.readResultsRethwisch()
				.filter(ballot -> ballot.getPollingStation()
						.getName()
						.equals(LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF));
		final LocalElection election = result.getElection();

		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(result, true);

		// @formatter:off
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getNumberOfVotes()).isEqualTo(124);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getNumberOfVotes()).isEqualTo(151);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getNumberOfVotes()).isEqualTo(82);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getNumberOfVotes()).isEqualTo(105);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getNumberOfVotes()).isEqualTo(169);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getNumberOfVotes()).isEqualTo(148);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getNumberOfVotes()).isEqualTo(52);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getNumberOfVotes()).isEqualTo(84);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getNumberOfVotes()).isEqualTo(133);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getNumberOfVotes()).isEqualTo(136);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getNumberOfVotes()).isEqualTo(27);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getNumberOfVotes()).isEqualTo(82);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getNumberOfVotes()).isEqualTo(46);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getNumberOfVotes()).isEqualTo(69);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getNumberOfVotes()).isEqualTo(109);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getNumberOfVotes()).isEqualTo(134);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getNumberOfVotes()).isEqualTo(222);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getNumberOfVotes()).isEqualTo(48);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getNumberOfVotes()).isEqualTo(58);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getNumberOfVotes()).isEqualTo(63);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getNumberOfVotes()).isEqualTo(68);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getNumberOfVotes()).isEqualTo(126);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getNumberOfVotes()).isEqualTo(73);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getNumberOfVotes()).isEqualTo(33);
		// @formatter:on
	}
}
