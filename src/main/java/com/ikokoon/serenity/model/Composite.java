package com.ikokoon.serenity.model;

import java.util.ArrayList;
import java.util.List;

public class Composite<E, F> extends AComposite<E, F> {

	private Long id;
	private IComposite<E, F> parent;
	private List<F> children = new ArrayList<F>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public IComposite<E, F> getParent() {
		return parent;
	}

	public void setParent(IComposite<E, F> parent) {
		this.parent = parent;
	}

	public List<F> getChildren() {
		return children;
	}

	public void setChildren(List<F> children) {
		this.children = children;
	}

}
