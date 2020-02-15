package de.larssh.election.germany.schleswigholstein.local.ui;

import java.util.Comparator;
import java.util.Optional;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.Election;
import de.larssh.election.germany.schleswigholstein.Nomination;
import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.Person;
import de.larssh.election.germany.schleswigholstein.local.LocalNomination;
import de.larssh.election.germany.schleswigholstein.local.LocalNominationType;
import de.larssh.election.germany.schleswigholstein.local.LocalPollingStation;
import de.larssh.utils.Nullables;
import de.larssh.utils.Optionals;
import de.larssh.utils.javafx.ChildController;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

@SuppressWarnings("PMD.ImmutableField")
public abstract class LocalDistrictUiController extends ChildController<LocalElectionController> {
	@FXML
	@NonFinal
	@Nullable
	TextField name = null;

	@FXML
	@NonFinal
	@Nullable
	ListView<LocalPollingStationListEntry> localPollingStations = null;

	@FXML
	@NonFinal
	@Nullable
	ChoiceBox<LocalNominationChoiceEntry> localNominations = null;

	@FXML
	@NonFinal
	@Nullable
	Pane localNomination = null;

	public LocalDistrictUiController(final LocalElectionController parent) {
		super(parent);
	}

	protected TextField getName() {
		return Nullables.orElseThrow(name);
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@EqualsAndHashCode
	public static class LocalPollingStationListEntry {
		@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
		private static final LocalPollingStationListEntry EMPTY = new LocalPollingStationListEntry(Optional.empty());

		public static LocalPollingStationListEntry empty() {
			return EMPTY;
		}

		@SuppressWarnings("PMD.ShortMethodName")
		public static LocalPollingStationListEntry of(final LocalPollingStation pollingStation) {
			return new LocalPollingStationListEntry(Optional.of(pollingStation));
		}

		Optional<LocalPollingStation> pollingStation;

		@Override
		public String toString() {
			return getPollingStation().map(LocalPollingStation::getName).orElse("<neuer Wahllokal>");
		}
	}

	@Getter
	@RequiredArgsConstructor
	@EqualsAndHashCode
	public static class DisplayableLocalNomination implements Nomination, Comparable<DisplayableLocalNomination> {
		private static final Comparator<DisplayableLocalNomination> COMPARATOR
				= Comparator.comparing(DisplayableLocalNomination::getParty, Optionals.comparator())
						.thenComparing(DisplayableLocalNomination::getOrderValue)
						.thenComparing(DisplayableLocalNomination::getPerson);

		LocalNominationType type;

		Optional<Party> party;

		int orderValue;

		Person person;

		@Override
		public int compareTo(@Nullable final DisplayableLocalNomination nomination) {
			return COMPARATOR.compare(this, nomination);
		}

		@Override
		public Election getElection() {
			throw new UnsupportedOperationException();
		}

		@Override
		public District<?> getDistrict() {
			throw new UnsupportedOperationException();
		}
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@EqualsAndHashCode
	public static class LocalNominationChoiceEntry {
		@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
		private static final LocalNominationChoiceEntry EMPTY = new LocalNominationChoiceEntry(Optional.empty());

		public static LocalNominationChoiceEntry empty() {
			return EMPTY;
		}

		@SuppressWarnings("PMD.ShortMethodName")
		public static LocalNominationChoiceEntry of(final DisplayableLocalNomination nomination) {
			return new LocalNominationChoiceEntry(Optional.of(nomination));
		}

		@SuppressWarnings("PMD.ShortMethodName")
		public static LocalNominationChoiceEntry of(final LocalNomination nomination) {
			return of(new DisplayableLocalNomination(nomination.getType(),
					nomination.getParty(),
					nomination.getElection().getNominations().indexOf(nomination),
					nomination.getPerson()));
		}

		Optional<DisplayableLocalNomination> nomination;

		@Override
		public String toString() {
			return getNomination().map(nomination -> String.format("%s: %s, %s",
					nomination.getParty().map(Party::getShortName).orElse("parteilos"),
					nomination.getPerson().getFamilyName(),
					nomination.getPerson().getGivenName())).orElse("<neuer Kandidat>");
		}
	}
}
