package de.larssh.election.germany.schleswigholstein.local;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.utils.annotations.PackagePrivate;

public class LocalPollingStation extends District<District<?>> {
	@PackagePrivate
	LocalPollingStation(final LocalDistrict parent, final String name) {
		super(Optional.of(parent), name);
	}

	@Override
	public District<?> createChild(@SuppressWarnings("unused") final String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	@JsonIgnore
	public Optional<District<?>> getParent() {
		return super.getParent();
	}

	@Override
	@JsonIgnore
	public LocalDistrictRoot getRoot() {
		return (LocalDistrictRoot) super.getRoot();
	}
}
