package com.ikokoon.serenity.instrumentation.dependency;

import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import com.ikokoon.serenity.ATest;

public class DependencyAnnotationAdapterTest extends ATest {

	@Test
	public void visit() {
		AnnotationVisitor annotationVisitor = new EmptyVisitor();
		String description = "Lcom/ikokoon/target/consumer/Annotation;(fields={\"name\"})";
		DependencyAnnotationAdapter annotationAdapter = new DependencyAnnotationAdapter(annotationVisitor, className, description);
		annotationAdapter.visit(description, "name");
		annotationAdapter.visitAnnotation(description, description);
	}

}
