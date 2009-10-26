package com.ikokoon.target;

import org.junit.Test;

import com.ikokoon.persistence.DataBaseDump;
import com.ikokoon.persistence.IDataBase;

/**
 * This is the test for the target test class.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class TargetTest {

	@Test
	public void getName() {
		Target target = new Target();
		for (int i = 0; i < 5; i++) {
			target.getName("Michael Couck");
		}
		target = new Target("Michael Couck");
		for (int i = 0; i < 23; i++) {
			target.getName("Michael Couck");
		}
	}

	@Test
	public void complexMethod() throws Exception {
		Target target = new Target();
		for (int i = 0; i < 5; i++) {
			target.complexMethod("s1", "s2", "s3", 1, 1);
		}
		for (int i = 0; i < 5; i++) {
			target.complexMethod("s" + i, "s" + (i - 1), "s" + (i - 2), 1, 1);
			target.complexMethod("s" + (i - 1), "s" + (i + 1), "s" + (i + 2), 1, 1);
		}
		target = new Target("Michael Couck");
		for (int i = 0; i < 5; i++) {
			target.complexMethod("s" + i, "s" + (i - 1), "s" + (i - 2), 1, 1);
			target.complexMethod("s" + (i - 1), "s" + (i + 1), "s" + (i + 2), 1, 1);
		}
	}
	
	@Test
	public void dump() {
		DataBaseDump.dump(IDataBase.DataBase.getDataBase());
	}

}