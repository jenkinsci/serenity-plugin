package com.ikokoon.serenity.model;

import java.util.List;

public interface IComposite<E, F> {

	public Long getId();

	public void setId(Long id);

	public IComposite<E, F> getParent();

	public void setParent(IComposite<E, F> parent);

	public List<F> getChildren();

	public void setChildren(List<F> children);

}
