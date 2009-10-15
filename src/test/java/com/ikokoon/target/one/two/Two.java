package com.ikokoon.target.one.two;

public class Two implements ITwo {

	public static interface ITwoTwo {
		public String getName();
	}

	public class TwoTwo implements ITwoTwo {
		private String name = "Michael";

		public String getName() {
			return name;
		}
	}

	public static class TwoTwoTwo implements ITwoTwo {
		private String name = "Michael";

		public String getName() {
			return name;
		}
	}

	private String name = "Michael";
	private ITwoTwo twoTwo = new TwoTwo();
	private ITwoTwo twoTwoTwo = new TwoTwoTwo();
	private static ITwoTwo twoTwoTwoTwo = new TwoTwoTwo();

	public String getName() {
		twoTwo.getName();
		twoTwoTwo.getName();
		twoTwoTwoTwo.getName();
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
