package com.ikokoon.serenity.instrumentation.profiling;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import com.ikokoon.serenity.IConstants;

/**
 * @author Michael Couck
 * @since 10.06.10
 * @version 01.00
 */
public abstract class ProfilingMethodAdviceAdapter extends AdviceAdapter {

	private static String OBJECT_CLASS_NAME = Type.getInternalName(Object.class);
	private static String THREAD_CLASS_NAME = Type.getInternalName(Thread.class);
	private static String SLEEP = "sleep";
	private static String WAIT = "wait";
	private static String JOIN = "join";
	private static String YIELD = "yield";

	public ProfilingMethodAdviceAdapter(MethodVisitor methodVisitor, int access, String methodName, String methodDescription) {
		super(methodVisitor, access, methodName, methodDescription);
	}

	protected boolean isWaitInsn(int opcode, String owner, String methodName, String methodDescription) {
		switch (opcode) {
		case Opcodes.INVOKESTATIC: {
			if (THREAD_CLASS_NAME.equals(owner)) {
				// "(J)V", "(JI)V"
				if (SLEEP.equals(methodName)
						&& (IConstants.sleepLongMethodDescriptor.equals(methodDescription) || IConstants.sleepLongIntMethodDescriptor
								.equals(methodDescription))) {
					return true;
				}
				// "()V"
				if (YIELD.equals(methodName) && IConstants.yieldMethodDescriptor.equals(methodDescription)) {
					return true;
				}
			}
		}
		case Opcodes.INVOKEVIRTUAL: {
			if (OBJECT_CLASS_NAME.equals(owner)) {
				if (WAIT.equals(methodName)
						&& (IConstants.waitMethodDescriptor.equals(methodDescription)
								|| IConstants.waitLongMethodDescriptor.equals(methodDescription) || IConstants.waitLongIntMethodDescriptor
								.equals(methodDescription))) {
					// "()V", "(J)V", "(JI)V"
					return true;
				}
			} else if (THREAD_CLASS_NAME.equals(owner)) {
				// "()V", "(J)V", "(JI)V"
				if (JOIN.equals(methodName)
						&& (IConstants.joinMethodDescriptor.equals(methodDescription)
								|| IConstants.joinLongMethodDescriptor.equals(methodDescription) || IConstants.joinLongIntMethodDescriptor
								.equals(methodDescription))) {
					return true;
				}
			}
		}
		}
		return false;
	}

}