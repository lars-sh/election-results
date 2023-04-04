package de.larssh.election.germany.schleswigholstein.local;

import java.nio.file.Paths;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
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
				Paths.get("../LocalNominationResult-all-one-finished.txt"));
		final Map<Party, LocalPartyResult> partyResults = result.getPartyResults();

		// Parties
		final Party cdu = LocalElectionTest.findParty(election, "CDU");
		final Party spd = LocalElectionTest.findParty(election, "SPD");
		final Party awg = LocalElectionTest.findParty(election, "AWG");
		final Party fwr = LocalElectionTest.findParty(election, "FWR");

		final SoftAssertions softAssertions = new SoftAssertions();

		// Votes
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfVotes()).isEqualTo(6);
		softAssertions.assertThat(partyResults.get(spd).getNumberOfVotes()).isEqualTo(6);
		softAssertions.assertThat(partyResults.get(awg).getNumberOfVotes()).isEqualTo(6);
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfVotes()).isEqualTo(6);

		// Block Votings
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfBlockVotings()).isZero();
		softAssertions.assertThat(partyResults.get(spd).getNumberOfBlockVotings()).isZero();
		softAssertions.assertThat(partyResults.get(awg).getNumberOfBlockVotings()).isZero();
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfBlockVotings()).isZero();

		// Certain Seats
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfCertainSeats()).isEqualTo(2);
		softAssertions.assertThat(partyResults.get(spd).getNumberOfCertainSeats()).isEqualTo(2);
		softAssertions.assertThat(partyResults.get(awg).getNumberOfCertainSeats()).isEqualTo(2);
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfCertainSeats()).isEqualTo(2);

		softAssertions.assertAll();
	}

	/**
	 * Test results in case no ballot was evaluated
	 */
	@Test
	@PackagePrivate
	void testAllZero() {
		final LocalElection election = LocalElectionTest.createElection();
		final LocalElectionResult result = LocalNominationResultTest.readResultKleinBoden(election,
				Paths.get("../LocalNominationResult-all-zero-finished.txt"));
		final Map<Party, LocalPartyResult> partyResults = result.getPartyResults();

		// Parties
		final Party cdu = LocalElectionTest.findParty(election, "CDU");
		final Party spd = LocalElectionTest.findParty(election, "SPD");
		final Party awg = LocalElectionTest.findParty(election, "AWG");
		final Party fwr = LocalElectionTest.findParty(election, "FWR");

		final SoftAssertions softAssertions = new SoftAssertions();

		// Votes
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfVotes()).isZero();
		softAssertions.assertThat(partyResults.get(spd).getNumberOfVotes()).isZero();
		softAssertions.assertThat(partyResults.get(awg).getNumberOfVotes()).isZero();
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfVotes()).isZero();

		// Block Votings
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfBlockVotings()).isZero();
		softAssertions.assertThat(partyResults.get(spd).getNumberOfBlockVotings()).isZero();
		softAssertions.assertThat(partyResults.get(awg).getNumberOfBlockVotings()).isZero();
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfBlockVotings()).isZero();

		// Certain Seats
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfCertainSeats()).isZero();
		softAssertions.assertThat(partyResults.get(spd).getNumberOfCertainSeats()).isZero();
		softAssertions.assertThat(partyResults.get(awg).getNumberOfCertainSeats()).isZero();
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfCertainSeats()).isZero();

		softAssertions.assertAll();
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
		final Party cdu = LocalElectionTest.findParty(election, "CDU");
		final Party spd = LocalElectionTest.findParty(election, "SPD");
		final Party awg = LocalElectionTest.findParty(election, "AWG");
		final Party fwr = LocalElectionTest.findParty(election, "FWR");

		final SoftAssertions softAssertions = new SoftAssertions();

		// Votes
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfVotes()).isEqualTo(417);
		softAssertions.assertThat(partyResults.get(spd).getNumberOfVotes()).isEqualTo(110);
		softAssertions.assertThat(partyResults.get(awg).getNumberOfVotes()).isEqualTo(108);
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfVotes()).isEqualTo(249);

		// Block Votings
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfBlockVotings()).isEqualTo(41);
		softAssertions.assertThat(partyResults.get(spd).getNumberOfBlockVotings()).isEqualTo(6);
		softAssertions.assertThat(partyResults.get(awg).getNumberOfBlockVotings()).isEqualTo(4);
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfBlockVotings()).isEqualTo(20);

		// Certain Seats
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfCertainSeats()).isEqualTo(1);
		softAssertions.assertThat(partyResults.get(spd).getNumberOfCertainSeats()).isZero();
		softAssertions.assertThat(partyResults.get(awg).getNumberOfCertainSeats()).isZero();
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfCertainSeats()).isEqualTo(1);

		softAssertions.assertAll();
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
		final Party cdu = LocalElectionTest.findParty(election, "CDU");
		final Party spd = LocalElectionTest.findParty(election, "SPD");
		final Party awg = LocalElectionTest.findParty(election, "AWG");
		final Party fwr = LocalElectionTest.findParty(election, "FWR");

		final SoftAssertions softAssertions = new SoftAssertions();

		// Votes
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfVotes()).isEqualTo(1322);
		softAssertions.assertThat(partyResults.get(spd).getNumberOfVotes()).isEqualTo(400);
		softAssertions.assertThat(partyResults.get(awg).getNumberOfVotes()).isEqualTo(787);
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfVotes()).isEqualTo(717);

		// Block Votings
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfBlockVotings()).isEqualTo(106);
		softAssertions.assertThat(partyResults.get(spd).getNumberOfBlockVotings()).isEqualTo(19);
		softAssertions.assertThat(partyResults.get(awg).getNumberOfBlockVotings()).isEqualTo(45);
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfBlockVotings()).isEqualTo(51);

		// Certain Seats
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfCertainSeats()).isEqualTo(5);
		softAssertions.assertThat(partyResults.get(spd).getNumberOfCertainSeats()).isEqualTo(1);
		softAssertions.assertThat(partyResults.get(awg).getNumberOfCertainSeats()).isEqualTo(3);
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfCertainSeats()).isEqualTo(2);

		softAssertions.assertAll();
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
		final Party cdu = LocalElectionTest.findParty(election, "CDU");
		final Party spd = LocalElectionTest.findParty(election, "SPD");
		final Party awg = LocalElectionTest.findParty(election, "AWG");
		final Party fwr = LocalElectionTest.findParty(election, "FWR");

		final SoftAssertions softAssertions = new SoftAssertions();

		// Votes
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfVotes()).isEqualTo(905);
		softAssertions.assertThat(partyResults.get(spd).getNumberOfVotes()).isEqualTo(290);
		softAssertions.assertThat(partyResults.get(awg).getNumberOfVotes()).isEqualTo(679);
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfVotes()).isEqualTo(468);

		// Block Votings
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfBlockVotings()).isEqualTo(65);
		softAssertions.assertThat(partyResults.get(spd).getNumberOfBlockVotings()).isEqualTo(13);
		softAssertions.assertThat(partyResults.get(awg).getNumberOfBlockVotings()).isEqualTo(41);
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfBlockVotings()).isEqualTo(31);

		// Certain Seats
		softAssertions.assertThat(partyResults.get(cdu).getNumberOfCertainSeats()).isEqualTo(3);
		softAssertions.assertThat(partyResults.get(spd).getNumberOfCertainSeats()).isEqualTo(1);
		softAssertions.assertThat(partyResults.get(awg).getNumberOfCertainSeats()).isEqualTo(2);
		softAssertions.assertThat(partyResults.get(fwr).getNumberOfCertainSeats()).isEqualTo(1);

		softAssertions.assertAll();
	}
}
