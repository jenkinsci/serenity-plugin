package com.ikokoon.serenity.hudson.modeller;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.target.Target;

/**
 * Tests the modeller that takes a composite and builds a model for it that can be displayed in the graph applet.
 *
 * @author Michael Couck
 * @since 17.11.09
 * @version 01.00
 */
public class ModellerTest extends ATest {

	@SuppressWarnings("rawtypes")
	@Test
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

		@SuppressWarnings("unused")
		Project projectThree = null;

		// TODO implement me
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

		@SuppressWarnings("unused")
		Package<?, ?> pakkageThree = null;

		// TODO implement me
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

		@SuppressWarnings("unused")
		Class<?, ?> klassThree = null;

		// TODO implement me
	}

}
