package com.ikokoon.persistence;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.ikokoon.ATest;
import com.ikokoon.instrumentation.model.Class;
import com.ikokoon.instrumentation.model.Line;
import com.ikokoon.instrumentation.model.Method;
import com.ikokoon.instrumentation.model.Package;
import com.ikokoon.instrumentation.model.Project;
import com.ikokoon.toolkit.Toolkit;

public class DataBaseDump extends ATest {

	@Test
	public void dump() {
		File file = new File("C:/Eclipse/workspace/serenity/serenity/serenity.db");
		IDataBase dataBase = IDataBase.DataBase.getDataBase(file);
		dump(dataBase);
	}

	public static void dump(IDataBase dataBase) {
		Project project = dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		List<com.ikokoon.instrumentation.model.Package> packages = project.getChildren();
		for (Package pakkage : packages) {
			logger.error("Package : " + pakkage + ", " + pakkage.getChildren().size());
			for (Class klass : pakkage.getChildren()) {
				logger.error("Class : " + klass + ", " + klass.getChildren());
				for (Method method : klass.getChildren()) {
					logger.error("Method : " + method + ", " + method.getChildren().size());
					for (Line line : method.getChildren()) {
						logger.error("Line : " + line);
					}
				}
			}
		}
	}

}
