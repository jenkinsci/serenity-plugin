package com.ikokoon.target.three;

import org.apache.log4j.Logger;

import com.ikokoon.target.Target;

public class Four extends AFour implements IFour {

	private Logger logger = Logger.getLogger(Four.class);

	public void execute() {
		Three three = new Three();
		three.execute();
	}

	public void execute(Target target) {
		try {
			target.complexMethod("s1", "s2", "s3", 1, 2);
			target.complexMethod("s", "s", "s", 1, 1);
		} catch (Exception e) {
			logger.error("Exception executing the target complex method", e);
		}
	}

}
