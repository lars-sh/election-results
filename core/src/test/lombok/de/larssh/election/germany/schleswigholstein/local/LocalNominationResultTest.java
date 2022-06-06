package de.larssh.election.germany.schleswigholstein.local;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.larssh.utils.annotations.PackagePrivate;
import lombok.NoArgsConstructor;

/**
 * {@link LegacyParser}
 */
@PackagePrivate
@NoArgsConstructor
class LocalNominationResultTest {
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

	@Test
	@PackagePrivate
	void testTypeAllZero() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result = LegacyParserTest
				.readResult(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN, Paths.get("all-zero.txt"));

		getAndAssertNominationResults(result, true).values()
				.forEach(nominationResult -> assertThat(nominationResult.getType())
						.isEqualTo(LocalNominationResultType.NOT_ELECTED));
	}

	@Test
	@PackagePrivate
	void testTypeAllOne() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result = LegacyParserTest
				.readResult(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN, Paths.get("all-one.txt"));

		getAndAssertNominationResults(result, true).values()
				.stream()
				.filter(nominationResult -> nominationResult.getNomination().getType() == LocalNominationType.DIRECT)
				.forEach(nominationResult -> assertThat(nominationResult.getType())
						.isEqualTo(LocalNominationResultType.DIRECT_DRAW));
	}

	@Test
	@PackagePrivate
	void testTypeBalanceAndOverhangSeats() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result = LegacyParserTest.readResult(election,
				LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN,
				Paths.get("balance-and-overhang-seats.txt"));

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
	 * Less list draw results than list positions, all direct draw positions
	 */
	@Test
	@PackagePrivate
	void testTypeClosedDraws1() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult resultWithoutDrawResults = LegacyParserTest
				.readResult(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN, Paths.get("draws.txt"));
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
	 * Less direct draw results than direct positions, all list draw positions
	 */
	@Test
	@PackagePrivate
	void testTypeClosedDraws2() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult resultWithoutDrawResults = LegacyParserTest
				.readResult(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN, Paths.get("draws.txt"));
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

	@Test
	@PackagePrivate
	void testTypeOpenDraws() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result = LegacyParserTest
				.readResult(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN, Paths.get("draws.txt"));

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
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getBallots()).hasSize(57);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getBallots()).hasSize(69);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getBallots()).hasSize(64);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getBallots()).hasSize(16);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getBallots()).hasSize(26);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getBallots()).hasSize(71);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getBallots()).hasSize(12);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getBallots()).hasSize(28);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getBallots()).hasSize(18);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getBallots()).hasSize(24);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getBallots()).hasSize(10);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getBallots()).hasSize(34);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getBallots()).hasSize(29);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getBallots()).hasSize(46);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getBallots()).hasSize(41);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getBallots()).hasSize(57);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getBallots()).hasSize(106);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getBallots()).hasSize(11);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getBallots()).hasSize(27);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getBallots()).hasSize(10);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getBallots()).hasSize(37);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getBallots()).hasSize(57);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getBallots()).hasSize(14);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getBallots()).hasSize(20);
		// @formatter:on
	}

	@Test
	@PackagePrivate
	void testVotesRethwisch() {
		final LocalElectionResult result = LegacyParserTest.readResultsRethwisch();
		final LocalElection election = result.getElection();

		final Map<LocalNomination, LocalNominationResult> nominationResults
				= getAndAssertNominationResults(result, true);

		// @formatter:off
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getBallots()).hasSize(181);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getBallots()).hasSize(220);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getBallots()).hasSize(146);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getBallots()).hasSize(121);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getBallots()).hasSize(195);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getBallots()).hasSize(219);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getBallots()).hasSize(64);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getBallots()).hasSize(112);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getBallots()).hasSize(151);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getBallots()).hasSize(160);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getBallots()).hasSize(37);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getBallots()).hasSize(116);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getBallots()).hasSize(75);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getBallots()).hasSize(115);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getBallots()).hasSize(150);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getBallots()).hasSize(191);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getBallots()).hasSize(328);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getBallots()).hasSize(59);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getBallots()).hasSize(85);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getBallots()).hasSize(73);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getBallots()).hasSize(105);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getBallots()).hasSize(183);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getBallots()).hasSize(87);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getBallots()).hasSize(53);
		// @formatter:on
	}

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
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getBallots()).hasSize(124);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getBallots()).hasSize(151);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getBallots()).hasSize(82);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getBallots()).hasSize(105);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getBallots()).hasSize(169);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getBallots()).hasSize(148);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getBallots()).hasSize(52);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getBallots()).hasSize(84);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getBallots()).hasSize(133);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getBallots()).hasSize(136);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getBallots()).hasSize(27);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getBallots()).hasSize(82);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getBallots()).hasSize(46);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getBallots()).hasSize(69);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getBallots()).hasSize(109);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getBallots()).hasSize(134);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getBallots()).hasSize(222);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getBallots()).hasSize(48);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getBallots()).hasSize(58);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getBallots()).hasSize(63);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getBallots()).hasSize(68);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getBallots()).hasSize(126);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getBallots()).hasSize(73);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getBallots()).hasSize(33);
		// @formatter:on
	}
}
