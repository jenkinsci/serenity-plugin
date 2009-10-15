package com.ikokoon.target.two;

public class Two implements ITwo {

	private IOne one;
	protected com.ikokoon.target.one.IOne anotherOne;
	protected com.ikokoon.target.one.Two two;

	public void setOne(IOne one) {
		this.one = one;
	}

	public IOne getOne() {
		return this.one;
	}

}
