package de.larssh.election.germany.schleswigholstein.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.local.file.PollingStationResultFilesTest;
import de.larssh.utils.annotations.PackagePrivate;
import lombok.NoArgsConstructor;

/**
 * {@link LocalPartyResult}
 */
@PackagePrivate
@NoArgsConstructor
class LocalPartyResultTest {
	/**
	 * Test results in case exactly the same number of votes were given to all
	 * nominations.
	 */
	@Test
	@PackagePrivate
	void testAllOne() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result = LocalNominationResultTest.readResultKleinBoden(election,
				Paths.get("../LocalNominationResult-all-one.txt"));
		final Map<Party, LocalPartyResult> partyResults = result.getPartyResults();

		// Parties
		final Party CDU = LocalElectionTest.findParty(election, "CDU");
		final Party SPD = LocalElectionTest.findParty(election, "SPD");
		final Party AWG = LocalElectionTest.findParty(election, "AWG");
		final Party FWR = LocalElectionTest.findParty(election, "FWR");

		// Votes
		assertThat(partyResults.get(CDU).getNumberOfVotes()).isEqualTo(6);
		assertThat(partyResults.get(SPD).getNumberOfVotes()).isEqualTo(6);
		assertThat(partyResults.get(AWG).getNumberOfVotes()).isEqualTo(6);
		assertThat(partyResults.get(FWR).getNumberOfVotes()).isEqualTo(6);

		// Block Votings
		assertThat(partyResults.get(CDU).getNumberOfBlockVotings()).isZero();
		assertThat(partyResults.get(SPD).getNumberOfBlockVotings()).isZero();
		assertThat(partyResults.get(AWG).getNumberOfBlockVotings()).isZero();
		assertThat(partyResults.get(FWR).getNumberOfBlockVotings()).isZero();

		// Certain Seats
		assertThat(partyResults.get(CDU).getNumberOfCertainSeats()).isZero();
		assertThat(partyResults.get(SPD).getNumberOfCertainSeats()).isZero();
		assertThat(partyResults.get(AWG).getNumberOfCertainSeats()).isZero();
		assertThat(partyResults.get(FWR).getNumberOfCertainSeats()).isZero();
	}

	/**
	 * Test results in case no ballot was evaluated
	 */
	@Test
	@PackagePrivate
	void testAllZero() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result = LocalNominationResultTest.readResultKleinBoden(election,
				Paths.get("../LocalNominationResult-all-zero.txt"));
		final Map<Party, LocalPartyResult> partyResults = result.getPartyResults();

		// Parties
		final Party CDU = LocalElectionTest.findParty(election, "CDU");
		final Party SPD = LocalElectionTest.findParty(election, "SPD");
		final Party AWG = LocalElectionTest.findParty(election, "AWG");
		final Party FWR = LocalElectionTest.findParty(election, "FWR");

		// Votes
		assertThat(partyResults.get(CDU).getNumberOfVotes()).isZero();
		assertThat(partyResults.get(SPD).getNumberOfVotes()).isZero();
		assertThat(partyResults.get(AWG).getNumberOfVotes()).isZero();
		assertThat(partyResults.get(FWR).getNumberOfVotes()).isZero();

		// Block Votings
		assertThat(partyResults.get(CDU).getNumberOfBlockVotings()).isZero();
		assertThat(partyResults.get(SPD).getNumberOfBlockVotings()).isZero();
		assertThat(partyResults.get(AWG).getNumberOfBlockVotings()).isZero();
		assertThat(partyResults.get(FWR).getNumberOfBlockVotings()).isZero();

		// Certain Seats
		assertThat(partyResults.get(CDU).getNumberOfCertainSeats()).isZero();
		assertThat(partyResults.get(SPD).getNumberOfCertainSeats()).isZero();
		assertThat(partyResults.get(AWG).getNumberOfCertainSeats()).isZero();
		assertThat(partyResults.get(FWR).getNumberOfCertainSeats()).isZero();
	}

	/**
	 * Test results using results of Klein Boden
	 */
	@Test
	@PackagePrivate
	void testKleinBoden() {
		final LocalElectionResult resultRethwisch = PollingStationResultFilesTest.readResultsRethwisch();
		final LocalElection election = resultRethwisch.getElection();
		final LocalElectionResult result = resultRethwisch.filterByDistrict(
				LocalElectionTest.findPollingStation(election, LocalElectionTest.POLLING_STATION_NAME_KLEIN_BODEN));
		final Map<Party, LocalPartyResult> partyResults = result.getPartyResults();

		// Parties
		final Party CDU = LocalElectionTest.findParty(election, "CDU");
		final Party SPD = LocalElectionTest.findParty(election, "SPD");
		final Party AWG = LocalElectionTest.findParty(election, "AWG");
		final Party FWR = LocalElectionTest.findParty(election, "FWR");

		// Votes
		assertThat(partyResults.get(CDU).getNumberOfVotes()).isEqualTo(417);
		assertThat(partyResults.get(SPD).getNumberOfVotes()).isEqualTo(110);
		assertThat(partyResults.get(AWG).getNumberOfVotes()).isEqualTo(108);
		assertThat(partyResults.get(FWR).getNumberOfVotes()).isEqualTo(249);

		// Block Votings
		assertThat(partyResults.get(CDU).getNumberOfBlockVotings()).isEqualTo(41);
		assertThat(partyResults.get(SPD).getNumberOfBlockVotings()).isEqualTo(6);
		assertThat(partyResults.get(AWG).getNumberOfBlockVotings()).isEqualTo(4);
		assertThat(partyResults.get(FWR).getNumberOfBlockVotings()).isEqualTo(20);

		// Certain Seats
		// TODO: We should probably use the max eligible voters to calculate the certain
		// seats here
		assertThat(partyResults.get(CDU).getNumberOfCertainSeats()).isZero();
		assertThat(partyResults.get(SPD).getNumberOfCertainSeats()).isZero();
		assertThat(partyResults.get(AWG).getNumberOfCertainSeats()).isZero();
		assertThat(partyResults.get(FWR).getNumberOfCertainSeats()).isZero();
	}

	/**
	 * Test results using results of Rethwisch
	 */
	@Test
	@PackagePrivate
	void testRethwisch() {
		final LocalElectionResult result = PollingStationResultFilesTest.readResultsRethwisch();
		final LocalElection election = result.getElection();
		final Map<Party, LocalPartyResult> partyResults = result.getPartyResults();

		// Parties
		final Party CDU = LocalElectionTest.findParty(election, "CDU");
		final Party SPD = LocalElectionTest.findParty(election, "SPD");
		final Party AWG = LocalElectionTest.findParty(election, "AWG");
		final Party FWR = LocalElectionTest.findParty(election, "FWR");

		// Votes
		assertThat(partyResults.get(CDU).getNumberOfVotes()).isEqualTo(1322);
		assertThat(partyResults.get(SPD).getNumberOfVotes()).isEqualTo(400);
		assertThat(partyResults.get(AWG).getNumberOfVotes()).isEqualTo(787);
		assertThat(partyResults.get(FWR).getNumberOfVotes()).isEqualTo(717);

		// Block Votings
		assertThat(partyResults.get(CDU).getNumberOfBlockVotings()).isEqualTo(106);
		assertThat(partyResults.get(SPD).getNumberOfBlockVotings()).isEqualTo(19);
		assertThat(partyResults.get(AWG).getNumberOfBlockVotings()).isEqualTo(45);
		assertThat(partyResults.get(FWR).getNumberOfBlockVotings()).isEqualTo(51);

		// Certain Seats
		assertThat(partyResults.get(CDU).getNumberOfCertainSeats()).isEqualTo(5);
		assertThat(partyResults.get(SPD).getNumberOfCertainSeats()).isEqualTo(1);
		assertThat(partyResults.get(AWG).getNumberOfCertainSeats()).isEqualTo(3);
		assertThat(partyResults.get(FWR).getNumberOfCertainSeats()).isEqualTo(2);
	}

	/**
	 * Test results using results of Rethwischdorf
	 */
	@Test
	@PackagePrivate
	void testRethwischdorf() {
		final LocalElectionResult resultRethwisch = PollingStationResultFilesTest.readResultsRethwisch();
		final LocalElection election = resultRethwisch.getElection();
		final LocalElectionResult result = resultRethwisch.filterByDistrict(
				LocalElectionTest.findPollingStation(election, LocalElectionTest.POLLING_STATION_NAME_RETHWISCHDORF));
		final Map<Party, LocalPartyResult> partyResults = result.getPartyResults();

		// Parties
		final Party CDU = LocalElectionTest.findParty(election, "CDU");
		final Party SPD = LocalElectionTest.findParty(election, "SPD");
		final Party AWG = LocalElectionTest.findParty(election, "AWG");
		final Party FWR = LocalElectionTest.findParty(election, "FWR");

		// Votes
		assertThat(partyResults.get(CDU).getNumberOfVotes()).isEqualTo(905);
		assertThat(partyResults.get(SPD).getNumberOfVotes()).isEqualTo(290);
		assertThat(partyResults.get(AWG).getNumberOfVotes()).isEqualTo(679);
		assertThat(partyResults.get(FWR).getNumberOfVotes()).isEqualTo(468);

		// Block Votings
		assertThat(partyResults.get(CDU).getNumberOfBlockVotings()).isEqualTo(65);
		assertThat(partyResults.get(SPD).getNumberOfBlockVotings()).isEqualTo(13);
		assertThat(partyResults.get(AWG).getNumberOfBlockVotings()).isEqualTo(41);
		assertThat(partyResults.get(FWR).getNumberOfBlockVotings()).isEqualTo(31);

		// Certain Seats
		assertThat(partyResults.get(CDU).getNumberOfCertainSeats()).isZero();
		assertThat(partyResults.get(SPD).getNumberOfCertainSeats()).isZero();
		assertThat(partyResults.get(AWG).getNumberOfCertainSeats()).isZero();
		assertThat(partyResults.get(FWR).getNumberOfCertainSeats()).isZero();
	}
}
