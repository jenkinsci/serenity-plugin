package com.ikokoon.target.one;

import com.ikokoon.target.two.Two;

public class One implements IOne {

	public interface ITwo {
		public String getName();
	}

	public class OneTwo implements ITwo {
		private String name = "Michael";

		public String getName() {
			return name;
		}
	}

	private Two two;
	private ITwo itwo = new OneTwo();
	private com.ikokoon.target.one.two.ITwo itwotwo = new com.ikokoon.target.one.two.Two();

	public void setTwo() {
		if (this.two == null) {
			this.two = new Two();
		}
		itwotwo.getName();
		this.two.getOne();
		itwo.getName();
	}

}
