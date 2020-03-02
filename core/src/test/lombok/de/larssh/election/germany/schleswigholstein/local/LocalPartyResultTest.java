package de.larssh.election.germany.schleswigholstein.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import de.larssh.election.germany.schleswigholstein.Party;
import lombok.NoArgsConstructor;

/**
 * {@link LegacyParser}
 */
@NoArgsConstructor
public class LocalPartyResultTest {
	@Test
	public void testKleinBoden() {
		final LocalElectionResult result = LegacyParserTest.readAllResults()
				.filter(ballot -> ballot.getPollingStation()
						.getName()
						.equals(LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN));
		final LocalElection election = result.getElection();
		final Map<Party, LocalPartyResult> partyResults = result.getPartyResults();

		// Parties
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "CDU")).getNumberOfVotes()).isEqualTo(417);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "SPD")).getNumberOfVotes()).isEqualTo(110);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "AWG")).getNumberOfVotes()).isEqualTo(108);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "FWR")).getNumberOfVotes()).isEqualTo(249);

		// Block Votings
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "CDU")).getNumberOfBlockVotings())
				.isEqualTo(41);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "SPD")).getNumberOfBlockVotings())
				.isEqualTo(6);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "AWG")).getNumberOfBlockVotings())
				.isEqualTo(4);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "FWR")).getNumberOfBlockVotings())
				.isEqualTo(20);
	}

	@Test
	public void testRethwisch() {
		final LocalElectionResult result = LegacyParserTest.readAllResults();
		final LocalElection election = result.getElection();
		final Map<Party, LocalPartyResult> partyResults = result.getPartyResults();

		// Parties
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "CDU")).getNumberOfVotes()).isEqualTo(1322);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "SPD")).getNumberOfVotes()).isEqualTo(400);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "AWG")).getNumberOfVotes()).isEqualTo(787);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "FWR")).getNumberOfVotes()).isEqualTo(717);

		// Block Votings
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "CDU")).getNumberOfBlockVotings())
				.isEqualTo(106);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "SPD")).getNumberOfBlockVotings())
				.isEqualTo(19);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "AWG")).getNumberOfBlockVotings())
				.isEqualTo(45);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "FWR")).getNumberOfBlockVotings())
				.isEqualTo(51);
	}

	@Test
	public void testRethwischdorf() {
		final LocalElectionResult result = LegacyParserTest.readAllResults()
				.filter(ballot -> ballot.getPollingStation()
						.getName()
						.equals(LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF));
		final LocalElection election = result.getElection();
		final Map<Party, LocalPartyResult> partyResults = result.getPartyResults();

		// Parties
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "CDU")).getNumberOfVotes()).isEqualTo(905);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "SPD")).getNumberOfVotes()).isEqualTo(290);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "AWG")).getNumberOfVotes()).isEqualTo(679);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "FWR")).getNumberOfVotes()).isEqualTo(468);

		// Block Votings
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "CDU")).getNumberOfBlockVotings())
				.isEqualTo(65);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "SPD")).getNumberOfBlockVotings())
				.isEqualTo(13);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "AWG")).getNumberOfBlockVotings())
				.isEqualTo(41);
		assertThat(partyResults.get(LocalElectionTest.findParty(election, "FWR")).getNumberOfBlockVotings())
				.isEqualTo(31);
	}
}
