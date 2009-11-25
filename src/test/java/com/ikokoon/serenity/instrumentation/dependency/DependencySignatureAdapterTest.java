package com.ikokoon.serenity.instrumentation.dependency;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.instrumentation.VisitorFactory;

public class DependencySignatureAdapterTest extends ATest {

	@Test
	public void visit() {
		String signature = "<E:Ljava/lang/Object;F:Ljava/lang/Object;>Ljava/lang/Object;Lcom/ikokoon/target/ITarget<TE;TF;>;Ljava/io/Serializable;";
		VisitorFactory.getSignatureVisitor(className, signature);
	}

}
