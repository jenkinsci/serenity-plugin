package com.ikokoon.serenity.hudson.modeller;

import com.ikokoon.serenity.model.IComposite;

public interface IModeller {

	public String getModel();

	public void visit(Class<?> klass, IComposite<?, ?>... composites);

}
