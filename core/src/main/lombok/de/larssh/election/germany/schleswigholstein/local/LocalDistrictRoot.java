package de.larssh.election.germany.schleswigholstein.local;

import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import de.larssh.election.germany.schleswigholstein.District;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, onParam_ = { @Nullable })
public class LocalDistrictRoot extends District<LocalDistrict> {
	LocalDistrictType type;

	@JsonCreator
	private LocalDistrictRoot(ParsableLocalDistrictRoot parseable) {
		this(parseable.getName(), parseable.getType());

		parseable.createChildrenFor(this);
	}

	public LocalDistrictRoot(final String name, final LocalDistrictType type) {
		super(Optional.empty(), name);

		this.type = type;
	}

	@Override
	public LocalDistrict createChild(final String name) {
		return addChild(new LocalDistrict(this, name));
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

	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalDistrictRoot {
		final String name;

		final Set<ParsableLocalDistrict> children;

		final LocalDistrictType type;

		public void createChildrenFor(LocalDistrictRoot localDistrictRoot) {
			for (ParsableLocalDistrict child : getChildren()) {
				child.createChildrenFor(localDistrictRoot.createChild(child.getName()));
			}
		}
	}

	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalDistrict {
		final String name;

		final Set<ParsableLocalPollingStation> children;

		public void createChildrenFor(LocalDistrict localDistrict) {
			getChildren().stream().map(ParsableLocalPollingStation::getName).forEach(localDistrict::createChild);
		}
	}

	@Getter
	@RequiredArgsConstructor
	private static class ParsableLocalPollingStation {
		final String name;
	}
}
