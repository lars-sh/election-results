package de.larssh.election.germany.schleswigholstein.local.ui;

import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.PartyType;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

public class PartyController extends PartyUiController {
	public PartyController(final LocalElectionController parent) {
		super(parent);
	}

	public Party getParty() {
		return new Party(getShortName().getText(),
				getName().getText(),
				getType().getValue(),
				getBackgroundColor().getValue(),
				getFontColor().getValue());
	}

	@Override
	protected void initialize() {
		getType().setItems(FXCollections.observableArrayList(PartyType.values()));

		reset();
	}

	public void reset() {
		getShortName().setText("");
		getName().setText("");
		getType().setValue(PartyType.POLITICAL_PARTY);
		getBackgroundColor().setValue(Color.WHITE);
		getFontColor().setValue(Color.BLACK);
	}

	public void setParty(final Party party) {
		getShortName().setText(party.getShortName());
		getName().setText(party.getName());
		getType().setValue(party.getType());
		getBackgroundColor().setValue(party.getBackgroundColor());
		getFontColor().setValue(party.getFontColor());
	}
}
