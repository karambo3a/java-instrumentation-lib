package org.instrumentation.agent;

import org.instrumentation.tracker.ConstantTracker;
import org.instrumentation.tracker.InstrEncoder;

import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.*;
import java.lang.constant.ConstantDesc;
import java.util.List;
import java.util.Set;

public class ConstantAgent implements Agent {
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
            private int branchNumber = 0;
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

                if (!(element instanceof Instruction elem)) {
                    return;
                }

                switch (element) {
                    case TableSwitchInstruction tableSwitchInstruction -> {
                        ++branchNumber;
                        long code = InstrEncoder.encode(classNumber, methodNumber, branchNumber);
                        List<ConstantDesc> constants = tableSwitchInstruction.cases().stream().map(switchCase -> (ConstantDesc) switchCase.caseValue()).toList();
                        ConstantTracker.addBranchConstants(code, constants);
                    }
                    case LookupSwitchInstruction lookupSwitchInstruction -> {
                        ++branchNumber;
                        long code = InstrEncoder.encode(classNumber, methodNumber, branchNumber);
                        List<ConstantDesc> constants = lookupSwitchInstruction.cases().stream().map(switchCase -> (ConstantDesc) switchCase.caseValue()).toList();
                        ConstantTracker.addBranchConstants(code, constants);
                    }
                    case BranchInstruction branchInstruction when (branchInstruction.opcode() != Opcode.GOTO && branchInstruction.opcode() != Opcode.GOTO_W) -> {
                        ++branchNumber;
                        var code = InstrEncoder.encode(classNumber, methodNumber, branchNumber);
                        if (Set.of(Opcode.IFEQ, Opcode.IFGE, Opcode.IFGT, Opcode.IFLE, Opcode.IFLT, Opcode.IFNE).contains(elem.opcode())) {
                            if (second instanceof LoadInstruction) {
                                ConstantTracker.addBranchConstants(code, List.of(0));
                            }
                        } else if (first instanceof ConstantInstruction f) {
                            ConstantTracker.addBranchConstants(code, List.of(f.constantValue()));
                        } else if (second instanceof ConstantInstruction s) {
                            ConstantTracker.addBranchConstants(code, List.of(s.constantValue()));
                        }
                    }
                    default -> {
                    }
                }

                first = second;
                second = elem;
            }
        }
        return new CodeTransformStateful(className, classNumber, methodModel, methodNumber, codeAttribute);
    }
}
