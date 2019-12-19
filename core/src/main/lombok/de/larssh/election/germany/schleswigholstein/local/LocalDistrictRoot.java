package de.larssh.election.germany.schleswigholstein.local;

import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.larssh.election.germany.schleswigholstein.District;
import de.larssh.election.germany.schleswigholstein.local.json.LocalDistrictRootDeserializer;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@JsonDeserialize(using = LocalDistrictRootDeserializer.class)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, onParam_ = { @Nullable })
public class LocalDistrictRoot extends District<LocalDistrict> {
	LocalDistrictType type;

	public LocalDistrictRoot(final String name, final LocalDistrictType type) {
		super(Optional.empty(), name);

		this.type = type;
	}

	@Override
	public LocalDistrict createChild(final String name) {
		return addChild(new LocalDistrict(this, name));
	}

	@Override
	public LocalDistrictRoot getRoot() {
		return (LocalDistrictRoot) super.getRoot();
	}
}
