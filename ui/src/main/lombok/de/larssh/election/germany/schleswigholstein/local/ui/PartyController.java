package de.larssh.election.germany.schleswigholstein.local.ui;

import java.util.Optional;

import de.larssh.election.germany.schleswigholstein.Party;
import de.larssh.election.germany.schleswigholstein.PartyType;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

public class PartyController extends PartyUiController {
	private static de.larssh.election.germany.schleswigholstein.Color colorFromJavaFx(final Color color) {
		return new de.larssh.election.germany.schleswigholstein.Color(color.getRed(),
				color.getGreen(),
				color.getBlue(),
				color.getOpacity());
	}

	private static Color colorToJavaFx(final de.larssh.election.germany.schleswigholstein.Color color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity());
	}

	public PartyController(final LocalElectionController parent) {
		super(parent);
	}

	public Party getParty() {
		return new Party(getShortName().getText(),
				getName().getText(),
				getType().getValue(),
				colorFromJavaFx(getBackgroundColor().getValue()),
				colorFromJavaFx(getFontColor().getValue()),
				Optional.empty());
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
		getBackgroundColor().setValue(colorToJavaFx(party.getBackgroundColor()));
		getFontColor().setValue(colorToJavaFx(party.getFontColor()));
	}
}
