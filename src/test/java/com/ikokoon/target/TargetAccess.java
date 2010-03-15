package com.ikokoon.target;

public abstract class TargetAccess {

	/** Classes. */
	public class PublicInnerClass {
	}

	class DefaultInnerClass {
	}

	protected class ProtectedInnerClass {
	}

	@SuppressWarnings("unused")
	private class PrivateInnerClass {
	}

	public static class PublicStaticInnerClass {
	}

	protected static class ProtectedStaticInnerClass {
	}

	static class DefaultStaticInnerClass {
	}

	@SuppressWarnings("unused")
	private static class PrivateStaticInnerClass {
	}

	/** Methods. */
	public void publicMethod() {
	}

	void defaultMethod() {
	}

	protected void protectedMethod() {
	}

	@SuppressWarnings("unused")
	private void privateMethod() {
	}

	public static void publicStaticMethod() {
	}

	static void defaultStaticMEthod() {
	}

	protected static void protectedStaticMethod() {
	}

	@SuppressWarnings("unused")
	private static void privateStaticMethod() {
	}

	public synchronized void synchronizedMethod() {
	}

	public abstract void abstractMethod();

}
