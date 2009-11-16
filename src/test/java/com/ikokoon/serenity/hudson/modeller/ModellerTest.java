package com.ikokoon.serenity.hudson.modeller;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.IModel;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.target.Target;
import com.ikokoon.toolkit.Toolkit;

public class ModellerTest extends ATest {

	@Test
	@SuppressWarnings("unchecked")
	public void visitProject() {
		Project projectOne = new Project();
		projectOne.setAbstractness(0.25);
		projectOne.setComplexity(17);
		projectOne.setCoverage(24);
		projectOne.setDistance(0.65);
		projectOne.setStability(0.36);

		projectOne.setLines(25045);
		projectOne.setMethods(523);
		projectOne.setClasses(542);
		projectOne.setPackages(51);

		Project projectTwo = new Project();
		projectTwo.setAbstractness(0.65);
		projectTwo.setComplexity(5);
		projectTwo.setCoverage(89);
		projectTwo.setDistance(0.33);
		projectTwo.setStability(0.93);

		projectTwo.setLines(65442);
		projectTwo.setMethods(2554);
		projectTwo.setClasses(854);
		projectTwo.setPackages(25);

		Modeller modeller = new Modeller();
		modeller.visit(Project.class, projectOne, projectTwo);

		String string = modeller.getModel();
		logger.debug(string);
		assertNotNull(string);

		// This is what we expect from the model
		IModel model = (IModel) Toolkit.deserializeFromBase64(string);
		assertEquals(
				"[[24.0, 89.0], [17.0, 5.0], [0.25, 0.65], [0.36, 0.93], [0.65, 0.33], [25045.0, 65442.0], [523.0, 2554.0], [542.0, 854.0], [51.0, 25.0]]",
				model.getMetrics().toString());
	}

	@Test
	public void visitPackage() {
		Package<?, ?> pakkageOne = getPackage();
		pakkageOne.setAbstractness(0.25);
		pakkageOne.setAfferent(17);
		pakkageOne.setComplexity(17);
		pakkageOne.setCoverage(63);
		pakkageOne.setDistance(0.68);
		pakkageOne.setEfferent(5);
		pakkageOne.setImplement(8);
		pakkageOne.setInterfaces(4);
		pakkageOne.setLines(1058);
		pakkageOne.setStability(0.66);
		pakkageOne.setExecuted(523645);

		Package<?, ?> pakkageTwo = getPackage();
		pakkageTwo.setName(Target.class.getPackage().getName());
		pakkageTwo.setAbstractness(0.36);
		pakkageTwo.setAfferent(5);
		pakkageTwo.setComplexity(59);
		pakkageTwo.setCoverage(23);
		pakkageTwo.setDistance(0.21);
		pakkageTwo.setEfferent(4);
		pakkageTwo.setImplement(542);
		pakkageTwo.setInterfaces(25);
		pakkageTwo.setLines(20225);
		pakkageTwo.setStability(0.05);
		pakkageTwo.setExecuted(5233658);

		Modeller modeller = new Modeller();
		modeller.visit(Package.class, pakkageOne, pakkageTwo);

		String string = modeller.getModel();
		assertNotNull(string);

		// This is what we expect from the model
		IModel model = (IModel) Toolkit.deserializeFromBase64(string);
		assertEquals(
				"[[63.0, 23.0], [17.0, 59.0], [0.25, 0.36], [0.66, 0.05], [0.68, 0.21], [1058.0, 20225.0], [4.0, 25.0], [8.0, 542.0], [523645.0, 5233658.0]]",
				model.getMetrics().toString());
	}

	@Test
	public void visitClass() {
		Class<?, ?> klassOne = getClass(getPackage());
		klassOne.setAfferent(5);
		klassOne.setComplexity(19);
		klassOne.setCoverage(81);
		klassOne.setEfferent(6);
		klassOne.setExecuted(2561);
		klassOne.setInterfaze(true);
		klassOne.setLines(256);
		klassOne.setStability(0.235);

		Class<?, ?> klassTwo = getClass(getPackage());
		klassTwo.setAfferent(98);
		klassTwo.setComplexity(21);
		klassTwo.setCoverage(05);
		klassTwo.setEfferent(65);
		klassTwo.setExecuted(61);
		klassTwo.setInterfaze(false);
		klassTwo.setLines(254);
		klassTwo.setStability(0.885);

		Modeller modeller = new Modeller();
		modeller.visit(Class.class, klassOne, klassTwo);

		String string = modeller.getModel();
		assertNotNull(string);

		// This is what we expect from the model
		IModel model = (IModel) Toolkit.deserializeFromBase64(string);
		assertEquals("[[81.0, 5.0], [19.0, 21.0], [0.23, 0.88], [256.0, 254.0], [2561.0, 61.0], [6.0, 65.0], [5.0, 98.0]]", model.getMetrics()
				.toString());
	}

}
