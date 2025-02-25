package org.instrumentation.agent;

import org.instrumentation.tracker.InstrEncoder;
import org.instrumentation.tracker.LineCoverageTracker;
import org.instrumentation.tracker.MethodInfo;

import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.LineNumber;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;

public class LineAgent implements Agent {
    @Override
    public ClassTransform createClassTransform(String className, long classNumber) {
        class ClassTransformStateful implements ClassTransform {
            private final String className;
            private final long classNumber;
            private long methodNumber = 0;

            public ClassTransformStateful(String className, long classNumber) {
                this.className = className;
                this.classNumber = classNumber;

                LineCoverageTracker.addClass(className);
                LineCoverageTracker.getMethods().add(new ArrayList<>());
            }

            @Override
            public void accept(ClassBuilder builder, ClassElement element) {
                if (element instanceof MethodModel methodModel) {
                    builder.transformMethod(methodModel, createMethodTransform(className, classNumber, methodModel, methodNumber++));
                } else {
                    builder.with(element);
                }
            }
        }
        return new ClassTransformStateful(className, classNumber);
    }

    @Override
    public MethodTransform createMethodTransform(String className, long classNumber, MethodModel methodModel, long methodNumber) {
        class MethodTransformStateful implements MethodTransform {
            private final String className;
            private final long classNumber;
            private final MethodModel methodModel;
            private final long methodNumber;

            public MethodTransformStateful(String className, long classNumber, MethodModel methodModel, long methodNumber) {
                this.className = className;
                this.classNumber = classNumber;
                this.methodModel = methodModel;
                this.methodNumber = methodNumber;

                LineCoverageTracker.addMethod(new MethodInfo(className, methodModel.methodName().stringValue(), methodModel.methodType().stringValue()));
            }

            @Override
            public void accept(MethodBuilder builder, MethodElement element) {
                if (element instanceof CodeModel codeModel && codeModel instanceof CodeAttribute codeAttribute) {
                    builder.transformCode(codeModel, createCodeTransform(className, classNumber, methodModel, methodNumber, codeAttribute));
                } else {
                    builder.with(element);
                }
            }
        }
        return new MethodTransformStateful(className, classNumber, methodModel, methodNumber);
    }

    @Override
    public CodeTransform createCodeTransform(String className, long classNumber, MethodModel methodModel, long methodNumber, CodeAttribute codeAttribute) {
        class CodeTransformStateful implements CodeTransform {
            private final String className;
            private final long classNumber;
            private final MethodModel methodModel;
            private final long methodNumber;
            private final CodeAttribute codeAttribute;

            public CodeTransformStateful(String className, long classNumber, MethodModel methodModel, long methodNumber, CodeAttribute codeAttribute) {
                this.className = className;
                this.classNumber = classNumber;
                this.methodModel = methodModel;
                this.methodNumber = methodNumber;
                this.codeAttribute = codeAttribute;
            }

            @Override
            public void accept(CodeBuilder builder, CodeElement element) {
                if (element instanceof LineNumber i) {
                    long code = InstrEncoder.encode(classNumber, methodNumber, i.line());
                    builder.
                            invokestatic(ClassDesc.of("org.instrumentation.tracker.LineCoverageTracker"), "getPrev", MethodTypeDesc.ofDescriptor("()J")).
                            ldc(code).
                            lcmp();
                    Label skipLabel = builder.newLabel();
                    builder.ifeq(skipLabel).
                            ldc(code)
                            .invokestatic(
                                    ClassDesc.of("org.instrumentation.tracker.LineCoverageTracker"),
                                    "logCoverage",
                                    MethodTypeDesc.ofDescriptor("(J)V")
                            );
                    builder.labelBinding(skipLabel);
                    LineCoverageTracker.logAllLine(code);
                    LineCoverageTracker.setPrev(code);
                }
                builder.with(element);
            }
        }
        return new CodeTransformStateful(className, classNumber, methodModel, methodNumber, codeAttribute);
    }
}
