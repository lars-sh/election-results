package de.larssh.election.germany.schleswigholstein.local;

import java.util.Optional;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.utils.annotations.PackagePrivate;

public class LocalDistrict extends District<LocalPollingStation> {
	@PackagePrivate
	LocalDistrict(final LocalDistrictSuper parent, final String name) {
		super(Optional.of(parent), name);
	}

	@Override
	public LocalPollingStation createChild(final String name) {
		return addChild(new LocalPollingStation(this, name));
	}
}
