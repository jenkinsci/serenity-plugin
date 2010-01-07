package com.ikokoon.target.discovery; // 1 - Discovery$1$1, Discovery$1, Discovery$2

public class Discovery { // 3 - Discovery

	private String name = "Michael Couck"; // 5 - Discovery

	public class InnerClass { // 7 - Discovery, Discovery$InnerClass
		public class InnerInnerClass { // 8 - Discovery$InnerClass$InnerInnerClass
			private String name = "Michael Couck"; // 9 - Covered - Discovery$InnerClass$InnerInnerClass

			public String getName() {
				return this.name; // 12 - Discovery$InnerClass$InnerInnerClass
			}
		}

		private String name = "Michael Couck"; // 16 - Discovery$InnerClass

		public String getName() {
			return this.name; // 19 - Discovery$InnerClass
		}
	}

	public String getName() {
		return this.name; // 24 - Discovery
	}

	public void getInnerClasses() {
		InnerClass innerClass = new InnerClass() { // 28 - Discovery, Discovery$1
			{
				new InnerClass() { // 30 - Discovery$1$1, Discovery$1
					public String getName() {
						return super.getName(); // 32 - Discovery$1$1
					}
				}.getName(); // 34 - Discovery$1
			}
		};
		innerClass.new InnerInnerClass() { // 37 - Discovery, Discovery$2, Discovery$2
		};
	} // 39 - Discovery

	public void getAnonymousInnerClass() {
		class AnotherInnerClass { // 42 - Discovery$1AnotherInnerClass
			private String name = "Michael Couck"; // 43 - Discovery$1AnotherInnerClass

			public String getName() {
				return name; // 46 - Discovery$1AnotherInnerClass
			}
		}
		AnotherInnerClass anotherInnerClass = new AnotherInnerClass(); // 49 - Discovery
		anotherInnerClass.getName(); // 50 - Discovery
	} // 51 - Discovery

}
/**
 * This class is for testing. NOTE if this class is changed in any way the unit tests need to be updated.
 * 
 * @author Michael Couck
 * @since 05.01.10
 * @version 01.00
 */
