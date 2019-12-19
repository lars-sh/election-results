package de.larssh.utils.javafx;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
public abstract class ChildController<T extends Controller> extends Controller {
	T parent;

	public ChildController(final T parent) {
		super(parent.getApplication(), parent.getStage());

		this.parent = parent;
	}
}
