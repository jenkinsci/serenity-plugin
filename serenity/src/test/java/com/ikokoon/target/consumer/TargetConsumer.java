package com.ikokoon.target.consumer;

import com.ikokoon.target.Target;

public class TargetConsumer {

	private Target<?, ?> target = new Target<Object, Object>(null);

	public Target<?, ?> getTarget() {
		return this.target;
	}
}
