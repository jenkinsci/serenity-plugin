package com.ikokoon.instrumentation.model;

import java.util.List;

public interface IComposite {

	public Long getId();

	public void setId(Long id);

	public IComposite getParent();

	public void setParent(IComposite parent);

	public List<IComposite> getChildren();

	public void setChildren(List<IComposite> children);

}
