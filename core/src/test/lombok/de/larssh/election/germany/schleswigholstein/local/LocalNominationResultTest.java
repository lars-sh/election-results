package de.larssh.election.germany.schleswigholstein.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;

import lombok.NoArgsConstructor;

/**
 * {@link LegacyParser}
 */
@NoArgsConstructor
public class LocalNominationResultTest {
	@Test
	public void testResultType() {
		final LocalElectionResult result = LegacyParserTest.readAllResults();
		final LocalElection election = result.getElection();

		// @formatter:off
		final Map<LocalNomination, LocalNominationResult> nominationResults = result.getNominationResults();
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
	public void testSainteLague() {
		final LocalElectionResult result = LegacyParserTest.readAllResults();
		final LocalElection election = result.getElection();

		// @formatter:off
		final Map<LocalNomination, LocalNominationResult> nominationResults = result.getNominationResults();
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(24036, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(88133, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Bernhardt", "Christian")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(12590, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(20486, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(31480, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(157400, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Dohrendorf", "Martina")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(13916, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Dohrendorf", "Thomas")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(9560, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Efrom", "Joachim")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(8435, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(52880, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(11429, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(26667, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Feddern", "Axel")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(7547, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Feddern", "Hartmut")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(6829, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gräpel", "Carola")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(20338, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(17489, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(52467, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Hartz", "Catrin")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(11031, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(16000, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Klein", "Erik")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(15553, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(28680, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(80000, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(13036, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(143400, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(37771, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(264400, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(7273, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schwarz", "Rupert")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(17627, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(15933, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(14309, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Topel", "Andreas")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(11496, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(47800, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(29378, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(22486, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getSainteLagueValue()).isEqualTo(BigDecimal.valueOf(8889, LocalElectionResult.SAINTE_LAGUE_SCALE_DEFAULT));
		// @formatter:on
	}

	@Test
	public void testVotesKleinBoden() {
		final LocalElectionResult result = LegacyParserTest.readAllResults()
				.filter(ballot -> ballot.getPollingStation()
						.getName()
						.equals(LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN));
		final LocalElection election = result.getElection();

		// @formatter:off
		final Map<LocalNomination, LocalNominationResult> nominationResults = result.getNominationResults();
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getBallots().size()).isEqualTo(57);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getBallots().size()).isEqualTo(69);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getBallots().size()).isEqualTo(64);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getBallots().size()).isEqualTo(16);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getBallots().size()).isEqualTo(26);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getBallots().size()).isEqualTo(71);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getBallots().size()).isEqualTo(12);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getBallots().size()).isEqualTo(28);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getBallots().size()).isEqualTo(18);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getBallots().size()).isEqualTo(24);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getBallots().size()).isEqualTo(10);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getBallots().size()).isEqualTo(34);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getBallots().size()).isEqualTo(29);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getBallots().size()).isEqualTo(46);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getBallots().size()).isEqualTo(41);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getBallots().size()).isEqualTo(57);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getBallots().size()).isEqualTo(106);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getBallots().size()).isEqualTo(11);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getBallots().size()).isEqualTo(27);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getBallots().size()).isEqualTo(10);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getBallots().size()).isEqualTo(37);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getBallots().size()).isEqualTo(57);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getBallots().size()).isEqualTo(14);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getBallots().size()).isEqualTo(20);
		// @formatter:on
	}

	@Test
	public void testVotesRethwisch() {
		final LocalElectionResult result = LegacyParserTest.readAllResults();
		final LocalElection election = result.getElection();

		// @formatter:off
		final Map<LocalNomination, LocalNominationResult> nominationResults = result.getNominationResults();
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getBallots().size()).isEqualTo(181);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getBallots().size()).isEqualTo(220);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getBallots().size()).isEqualTo(146);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getBallots().size()).isEqualTo(121);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getBallots().size()).isEqualTo(195);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getBallots().size()).isEqualTo(219);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getBallots().size()).isEqualTo(64);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getBallots().size()).isEqualTo(112);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getBallots().size()).isEqualTo(151);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getBallots().size()).isEqualTo(160);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getBallots().size()).isEqualTo(37);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getBallots().size()).isEqualTo(116);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getBallots().size()).isEqualTo(75);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getBallots().size()).isEqualTo(115);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getBallots().size()).isEqualTo(150);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getBallots().size()).isEqualTo(191);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getBallots().size()).isEqualTo(328);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getBallots().size()).isEqualTo(59);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getBallots().size()).isEqualTo(85);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getBallots().size()).isEqualTo(73);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getBallots().size()).isEqualTo(105);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getBallots().size()).isEqualTo(183);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getBallots().size()).isEqualTo(87);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getBallots().size()).isEqualTo(53);
		// @formatter:on
	}

	@Test
	public void testVotesRethwischdorf() {
		final LocalElectionResult result = LegacyParserTest.readAllResults()
				.filter(ballot -> ballot.getPollingStation()
						.getName()
						.equals(LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF));
		final LocalElection election = result.getElection();

		// @formatter:off
		final Map<LocalNomination, LocalNominationResult> nominationResults = result.getNominationResults();
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Beck", "Karsten")).getBallots().size()).isEqualTo(124);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Behnk", "Sönke")).getBallots().size()).isEqualTo(151);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Breede", "Rolf")).getBallots().size()).isEqualTo(82);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Johannes")).getBallots().size()).isEqualTo(105);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Böttger", "Volker")).getBallots().size()).isEqualTo(169);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eggers", "Dirk")).getBallots().size()).isEqualTo(148);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ehlert", "Armin")).getBallots().size()).isEqualTo(52);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Eick", "Ernst")).getBallots().size()).isEqualTo(84);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Henning")).getBallots().size()).isEqualTo(133);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Gäde", "Jan-Hendrik")).getBallots().size()).isEqualTo(136);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Jögimar", "Helga")).getBallots().size()).isEqualTo(27);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kraus", "Michael")).getBallots().size()).isEqualTo(82);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kröger", "Dirk")).getBallots().size()).isEqualTo(46);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "König", "Eva-Maria")).getBallots().size()).isEqualTo(69);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Kühn", "Steffen")).getBallots().size()).isEqualTo(109);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Motzkus", "Dietrich")).getBallots().size()).isEqualTo(134);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Poppinga", "Jens")).getBallots().size()).isEqualTo(222);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Sauer", "Joachim")).getBallots().size()).isEqualTo(48);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Schöning", "Mathias")).getBallots().size()).isEqualTo(58);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Stapelfeldt", "Albert")).getBallots().size()).isEqualTo(63);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Wahl", "Joachim")).getBallots().size()).isEqualTo(68);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Weger", "Marcel")).getBallots().size()).isEqualTo(126);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Winter", "Martin")).getBallots().size()).isEqualTo(73);
		assertThat(nominationResults.get(LocalElectionTest.findNomination(election, "Ziebarth", "Angelika")).getBallots().size()).isEqualTo(33);
		// @formatter:on
	}
}
