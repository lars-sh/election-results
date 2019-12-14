package de.larssh.utils.javafx;

import lombok.Getter;

@Getter
public abstract class ChildController<T extends Controller> extends Controller {
	T parent;

	public ChildController(final T parent) {
		super(parent.getApplication(), parent.getStage());

		this.parent = parent;
	}
}
