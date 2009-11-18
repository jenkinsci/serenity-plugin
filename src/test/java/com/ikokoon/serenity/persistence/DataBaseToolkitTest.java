package com.ikokoon.serenity.persistence;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.toolkit.Toolkit;

public class DataBaseToolkitTest extends ATest {

	@Test
	public void test() {
	}

	public static void clear(IDataBase dataBase) {
		List<Long> ids = new ArrayList<Long>();
		Project<?, ?> project = (Project<?, ?>) dataBase.find(Toolkit.hash(Project.class.getName()));
		if (project == null) {
			return;
		}
		for (Package<?, ?> pakkage : ((List<Package<?, ?>>) project.getChildren())) {
			ids.add(pakkage.getId());
			for (Class<?, ?> klass : ((List<Class<?, ?>>) pakkage.getChildren())) {
				ids.add(klass.getId());
				for (Method<?, ?> method : ((List<Method<?, ?>>) klass.getChildren())) {
					ids.add(method.getId());
					for (Line<?, ?> line : ((List<Line<?, ?>>) method.getChildren())) {
						ids.add(line.getId());
					}
				}
			}
		}
		for (Long id : ids) {
			dataBase.remove(id);
		}
	}

	public static void dump(IDataBase dataBase) {
		Project<?, ?> project = (Project<?, ?>) dataBase.find(Toolkit.hash(Project.class.getName()));
		logger.warn("Project : " + project.getName());
		for (Package<?, ?> pakkage : ((List<Package<?, ?>>) project.getChildren())) {
			logger.warn("\tPackage : " + pakkage.getId() + " : " + pakkage.getName());
			for (Class<?, ?> klass : ((List<Class<?, ?>>) pakkage.getChildren())) {
				logger.warn("\t\tClass : " + klass.getId() + " : " + klass.getName());
				for (Method<?, ?> method : ((List<Method<?, ?>>) klass.getChildren())) {
					logger.warn("\t\t\tMethod : " + method.getId() + " : " + method.getName());
					for (Line<?, ?> line : ((List<Line<?, ?>>) method.getChildren())) {
						logger.warn("\t\t\t\tLine : " + line.getId() + " : " + line.getNumber());
						line.setCounter(0d);
					}
				}
			}
		}
	}

}
