package com.ikokoon.instrumentation.model;

import java.util.List;

public interface IComposite<E> {

	public Long getId();

	public void setId(Long id);

	public IComposite getParent();

	public void setParent(IComposite parent);

	public List<E> getChildren();

	public void setChildren(List<E> children);

}
