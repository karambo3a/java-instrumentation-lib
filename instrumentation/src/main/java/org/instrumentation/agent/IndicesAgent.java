package org.instrumentation.agent;

import org.instrumentation.tracker.IndicesTracker;
import org.instrumentation.tracker.InstrEncoder;

import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.*;
import java.util.List;

public class IndicesAgent implements Agent {
    @Override
    public ClassTransform createClassTransform(String className, long classNumber) {
        class ClassTransformStateful implements ClassTransform {
            private final String className;
            private final long classNumber;
            private long methodNumber = 0;

            public ClassTransformStateful(String className, long classNumber) {
                this.className = className;
                this.classNumber = classNumber;
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
            private int arrayNumber = 0;
            private Instruction first = null;
            private Instruction second = null;


            public CodeTransformStateful(String className, long classNumber, MethodModel methodModel, long methodNumber, CodeAttribute codeAttribute) {
                this.className = className;
                this.classNumber = classNumber;
                this.methodModel = methodModel;
                this.methodNumber = methodNumber;
                this.codeAttribute = codeAttribute;
            }

            public void accept(CodeBuilder builder, CodeElement element) {
                builder.with(element);

                if (!(element instanceof Instruction instruction)) {
                    return;
                }

                ++arrayNumber;
                long code = InstrEncoder.encode(classNumber, methodNumber, arrayNumber);
                if (instruction instanceof ArrayLoadInstruction && second instanceof ConstantInstruction s) {
                    IndicesTracker.addArrayIndices(code, List.of(s.constantValue()));
                } else if (instruction instanceof ArrayStoreInstruction && first instanceof ConstantInstruction f) {
                    IndicesTracker.addArrayIndices(code, List.of(f.constantValue()));
                }

                first = second;
                second = instruction;
            }
        }
        return new CodeTransformStateful(className, classNumber, methodModel, methodNumber, codeAttribute);
    }
}
