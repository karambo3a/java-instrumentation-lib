package org.instrumentation.agent;

import org.instrumentation.tracker.BranchCoverageTracker;
import org.instrumentation.tracker.InstrEncoder;
import org.instrumentation.tracker.MethodInfo;

import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BranchAgent implements Agent {
    @Override
    public ClassTransform createClassTransform(String className, long classNumber) {
        class ClassTransformStateful implements ClassTransform {
            private final String className;
            private final long classNumber;
            private long methodNumber = 0;

            public ClassTransformStateful(String className, long classNumber) {
                this.className = className;
                this.classNumber = classNumber;

                BranchCoverageTracker.addClass(className);
                BranchCoverageTracker.getMethods().add(new ArrayList<>());
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

                BranchCoverageTracker.addMethod(new MethodInfo(className, methodModel.methodName().stringValue(), methodModel.methodType().stringValue()));
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
            private List<int[]> tableSwitch = new ArrayList<>();
            private List<int[]> lookUpSwitch = new ArrayList<>();
            private int branchNumber = 0;
            private int lineNumber = 0;

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

                if (element instanceof TableSwitchInstruction || element instanceof LookupSwitchInstruction) {
                    ++branchNumber;
                    if (element instanceof TableSwitchInstruction tableSwitchInstruction) {
                        tableSwitch = createSwitchTable(tableSwitchInstruction);
                    } else {
                        LookupSwitchInstruction lookupSwitchInstruction = (LookupSwitchInstruction) element;
                        lookUpSwitch = createLookUpTable(lookupSwitchInstruction);
                    }
                    long code = InstrEncoder.encode(classNumber, methodNumber, branchNumber);
                    BranchCoverageTracker.logAllBranch(code);
                }

                int[] lines = null;
                if (tableSwitch.stream().anyMatch(l -> l[1] == lineNumber)) {
                    lines = tableSwitch.stream().filter(l -> l[1] == lineNumber).findFirst().orElseThrow();
                } else if (lookUpSwitch.stream().anyMatch(l -> l[1] == lineNumber)) {
                    lines = lookUpSwitch.stream().filter(l -> l[1] == lineNumber).findFirst().orElseThrow();
                }
                if (lines != null) {
                    long code = InstrEncoder.encode(classNumber, methodNumber, lines[0]);
                    builder.ldc(code)
                            .invokestatic(
                                    ClassDesc.of("org.instrumentation.tracker.BranchCoverageTracker"),
                                    "logCoverage",
                                    MethodTypeDesc.ofDescriptor("(J)V")
                            );
                }

                if (element instanceof BranchInstruction branchInstruction && (branchInstruction.opcode() != Opcode.GOTO && branchInstruction.opcode() != Opcode.GOTO_W)) {
                    ++branchNumber;
                    long code = InstrEncoder.encode(classNumber, methodNumber, branchNumber);
                    builder.ldc(code)
                            .invokestatic(
                                    ClassDesc.of("org.instrumentation.tracker.BranchCoverageTracker"),
                                    "logCoverage",
                                    MethodTypeDesc.ofDescriptor("(J)V")
                            );
                    BranchCoverageTracker.logAllBranch(code);
                    BranchCoverageTracker.setPrev(code);
                }

                lineNumber += instruction.sizeInBytes();
            }

            private List<int[]> createSwitchTable(TableSwitchInstruction instruction) {
                return instruction.cases().stream().map(i ->
                        new int[]{branchNumber, codeAttribute.labelToBci(i.target())}
                ).collect(Collectors.toList());
            }

            private List<int[]> createLookUpTable(LookupSwitchInstruction instruction) {
                return instruction.cases().stream().map(i ->
                        new int[]{branchNumber, codeAttribute.labelToBci(i.target())}
                ).collect(Collectors.toList());
            }
        }
        return new CodeTransformStateful(className, classNumber, methodModel, methodNumber, codeAttribute);
    }
}
