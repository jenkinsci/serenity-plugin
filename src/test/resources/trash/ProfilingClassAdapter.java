package trash;

import com.ikokoon.serenity.IConstants;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * This class is not used. It has been replaced with the advice adapter class.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 30.09.09
 */
public class ProfilingClassAdapter extends ClassVisitor implements Opcodes {

    private String className;

    public ProfilingClassAdapter(ClassVisitor visitor, String className) {
        super(Opcodes.ASM5, visitor);
        this.className = className;
    }

    public MethodVisitor visitMethod(int access, final String methodName, final String methodDescription, String methodSignature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, methodName, methodDescription, methodSignature, exceptions);
        // MethodVisitor methodVisitor, Integer access, String className, String methodName, String methodDescription
        if (methodName.equals("<init>") || methodName.equals("<clinit>")) {
            return new AdviceAdapter(Opcodes.ASM5, methodVisitor, access, methodName, methodDescription) {
                @Override
                protected void onMethodEnter() {
                    this.visitLdcInsn(className);
                    this.visitLdcInsn(methodName);
                    this.visitLdcInsn(methodDescription);
                    this.visitMethodInsn(Opcodes.INVOKESTATIC, IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_START,
                            IConstants.PROFILING_METHOD_DESCRIPTION);
                }

                @Override
                protected void onMethodExit(int opcode) {
                    this.visitLdcInsn(className);
                    this.visitLdcInsn(methodName);
                    this.visitLdcInsn(methodDescription);
                    this.visitMethodInsn(Opcodes.INVOKESTATIC, IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_END,
                            IConstants.PROFILING_METHOD_DESCRIPTION);
                }
            };
        }
        return new ProfilingMethodAdapter(methodVisitor, access, className, methodName, methodDescription);
    }

}
