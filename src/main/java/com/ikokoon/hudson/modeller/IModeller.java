package com.ikokoon.hudson.modeller;

import com.ikokoon.instrumentation.model.IComposite;

public interface IModeller {

	public String getModel();

	public void visit(IComposite<?, ?> composite);

}
