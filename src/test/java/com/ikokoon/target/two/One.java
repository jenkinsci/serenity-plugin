package com.ikokoon.target.two;

import com.ikokoon.target.one.Two;

public class One implements IOne {
	
	protected ITwo two;
	protected com.ikokoon.target.one.One one;
	protected Two anotherTwo;

	public void setTwo(ITwo two) {
		this.two = two;
	}
	
}
