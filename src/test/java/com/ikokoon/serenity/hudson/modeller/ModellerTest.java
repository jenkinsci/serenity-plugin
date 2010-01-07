package com.ikokoon.serenity.hudson.modeller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.IModel;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.target.Target;
import com.ikokoon.toolkit.Toolkit;

/**
 * Tests the modeller that takes a composite and builds a model for it that can be displayed in the graph applet.
 * 
 * @author Michael Couck
 * @since 17.11.09
 * @version 01.00
 */
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

		Project projectThree = null;

		Modeller modeller = new Modeller();
		modeller.visit(Project.class, projectOne, projectTwo, projectThree);

		String string = modeller.getModel();
		assertNotNull(string);

		// This is what we expect from the model
		IModel model = (IModel) Toolkit.deserializeFromBase64(string);
		// Coverage, complexity, abstractness, stability, distance
		assertEquals("[[24.0, 89.0, 0.0], [17.0, 5.0, 0.0], [25.0, 65.0, 0.0], [36.0, 93.0, 0.0], [65.0, 33.0, 0.0]]", model.getMetrics().toString());
	}

	@Test
	public void visitPackage() {
		Package<?, ?> pakkageOne = getPackage();
		pakkageOne.setAbstractness(0.25);
		pakkageOne.setAfference(17);
		pakkageOne.setComplexity(17);
		pakkageOne.setCoverage(63);
		pakkageOne.setDistance(0.68);
		pakkageOne.setEfference(5);
		pakkageOne.setImplementations(8);
		pakkageOne.setInterfaces(4);
		pakkageOne.setLines(1058);
		pakkageOne.setStability(0.66);
		pakkageOne.setExecuted(523645);

		Package<?, ?> pakkageTwo = getPackage();
		pakkageTwo.setName(Target.class.getPackage().getName());
		pakkageTwo.setAbstractness(0.36);
		pakkageTwo.setAfference(5);
		pakkageTwo.setComplexity(59);
		pakkageTwo.setCoverage(23);
		pakkageTwo.setDistance(0.21);
		pakkageTwo.setEfference(4);
		pakkageTwo.setImplementations(542);
		pakkageTwo.setInterfaces(25);
		pakkageTwo.setLines(20225);
		pakkageTwo.setStability(0.05);
		pakkageTwo.setExecuted(5233658);

		Package<?, ?> pakkageThree = null;

		Modeller modeller = new Modeller();
		modeller.visit(Package.class, pakkageOne, pakkageTwo, pakkageThree);

		String string = modeller.getModel();
		assertNotNull(string);

		// This is what we expect from the model
		IModel model = (IModel) Toolkit.deserializeFromBase64(string);
		assertEquals("[[63.0, 23.0, 0.0], [17.0, 59.0, 0.0], [25.0, 36.0, 0.0], [66.0, 5.0, 0.0], [68.0, 21.0, 0.0]]", model.getMetrics().toString());
	}

	@Test
	public void visitClass() {
		Class<?, ?> klassOne = getClass(getPackage());
		klassOne.setAfference(5);
		klassOne.setComplexity(19);
		klassOne.setCoverage(81);
		klassOne.setEfference(6);
		klassOne.setInterfaze(true);
		klassOne.setStability(0.235);

		Class<?, ?> klassTwo = getClass(getPackage());
		klassTwo.setAfference(98);
		klassTwo.setComplexity(21);
		klassTwo.setCoverage(05);
		klassTwo.setEfference(65);
		klassTwo.setInterfaze(false);
		klassTwo.setStability(0.885);

		Class<?, ?> klassThree = null;

		Modeller modeller = new Modeller();
		modeller.visit(Class.class, klassOne, klassTwo, klassThree);

		String string = modeller.getModel();
		assertNotNull(string);

		// This is what we expect from the model
		IModel model = (IModel) Toolkit.deserializeFromBase64(string);
		assertEquals("[[81.0, 5.0, 0.0], [19.0, 21.0, 0.0], [23.5, 88.5, 0.0]]", model.getMetrics().toString());
	}

}
