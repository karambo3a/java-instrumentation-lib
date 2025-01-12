package org.instrumentation.agent;

import org.instrumentation.tracker.BranchCoverageTracker;
import org.instrumentation.tracker.CoverageTracker;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BranchAgent {
    private static Integer classNumber = 0;

    public static void premain(String agentArgs, Instrumentation inst) {
        Set<String> classes = Set.of(agentArgs.split(","));

//      adds branch coverage tracker
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classFileBuffer) {
                if (!classes.contains(className)) {
                    return classFileBuffer;
                }

                classNumber++;
                BranchCoverageTracker.classes.add(className);
                BranchCoverageTracker.methods.add(new ArrayList<>());
                ClassTransform classTransform = new ClassTransformStateful(classNumber, className);

                var oldClassFile = ClassFile.of().parse(classFileBuffer);
                var newClassFile = ClassFile.of().transform(oldClassFile, classTransform);
                saveClassFile(newClassFile);
                return newClassFile;
            }

            private void saveClassFile(byte[] classFile) {
                try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream("Example.class"))) {
                    outputStream.write(classFile);
                    outputStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static class ClassTransformStateful implements ClassTransform {
        private final int classNumber;
        private int methodNumber = 0;
        private final String className;

        public ClassTransformStateful(Integer classCnt, String className) {
            this.classNumber = classCnt;
            this.className = className;
        }

        @Override
        public void accept(ClassBuilder builder, ClassElement element) {
            if (element instanceof MethodModel methodModel) {
                ++methodNumber;
                BranchCoverageTracker.methods.get(classNumber - 1).add(this.className + '.' + methodModel.methodName().stringValue() + methodModel.methodType().stringValue());
                builder.transformMethod(methodModel, new MethodTransformStateful(methodModel, methodNumber, classNumber));
            } else {
                builder.with(element);
            }
        }
    }

    private record MethodTransformStateful(MethodModel methodModel, Integer methodCnt,
                                           Integer classCnt) implements MethodTransform {

        @Override
        public void accept(MethodBuilder builder, MethodElement element) {
            if (element instanceof CodeModel codeModel) {
                builder.transformCode(codeModel, new CodeTransformStateful((CodeAttribute) codeModel, methodCnt, classCnt));
            } else {
                builder.with(element);
            }
        }
    }

    private static class CodeTransformStateful implements CodeTransform {
        private final CodeAttribute codeModel;
        private final Integer methodNumber;
        private final Integer classNumber;
        private List<Integer[]> tableSwitch = new ArrayList<>();
        private List<Integer[]> lookUpSwitch = new ArrayList<>();
        private int branchNumber = 0;
        private int lineNumber = 0;
        private Instruction first = null;
        private Instruction second = null;


        public CodeTransformStateful(CodeAttribute codeModel, Integer methodCnt, Integer classCnt) {
            this.codeModel = codeModel;
            this.methodNumber = methodCnt;
            this.classNumber = classCnt;
        }


        @Override
        public void accept(CodeBuilder builder, CodeElement element) {
            if (element instanceof TableSwitchInstruction || element instanceof LookupSwitchInstruction) {
                ++branchNumber;
                List<ConstantDesc> switchConstants;
                if (element instanceof TableSwitchInstruction instruction) {
                    createSwitchTable(instruction);
                    switchConstants = instruction.cases().stream().map(switchCase -> (ConstantDesc) switchCase.caseValue()).toList();
                } else {
                    LookupSwitchInstruction instruction = (LookupSwitchInstruction) element;
                    createLookUpTable(instruction);
                    switchConstants = instruction.cases().stream().map(switchCase -> (ConstantDesc) switchCase.caseValue()).toList();
                }
                Long code = CoverageTracker.code(classNumber, methodNumber, branchNumber);
                BranchCoverageTracker.logAllBranch(code);
                BranchCoverageTracker.branchConstants.put(code, switchConstants);
            }

            builder.with(element);

            if (element instanceof Instruction) {
                Integer[] lines = null;
                if (tableSwitch.stream().anyMatch(l -> l[1] == lineNumber)) {
                    lines = tableSwitch.stream().filter(l -> l[1] == lineNumber).findFirst().get();
                } else if (lookUpSwitch.stream().anyMatch(l -> l[1] == lineNumber)) {
                    lines = lookUpSwitch.stream().filter(l -> l[1] == lineNumber).findFirst().get();
                }
                if (lines != null) {
                    Long code = CoverageTracker.code(classNumber, methodNumber, branchNumber);
                    builder.ldc(code.toString())
                            .invokestatic(
                                    ClassDesc.of("org.instrumentation.tracker.BranchCoverageTracker"),
                                    "logCoverage",
                                    MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)V")
                            );
                }
            }

            if (element instanceof BranchInstruction elem && (elem.opcode() != Opcode.GOTO && elem.opcode() != Opcode.GOTO_W)) {
                ++branchNumber;
                Long code = CoverageTracker.code(classNumber, methodNumber, branchNumber);
                builder.ldc(code.toString())
                        .invokestatic(
                                ClassDesc.of("org.instrumentation.tracker.BranchCoverageTracker"),
                                "logCoverage",
                                MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)V")
                        );
                BranchCoverageTracker.logAllBranch(code);
                saveBranchConstant(elem, code);
            }

            if (element instanceof Instruction elem) {
                lineNumber += elem.sizeInBytes();
                first = second;
                second = elem;
            }
        }

        private void createSwitchTable(TableSwitchInstruction instruction) {
            tableSwitch = instruction.cases().stream().map(i ->
                    new Integer[]{branchNumber, codeModel.labelToBci(i.target())}
            ).collect(Collectors.toList());
        }

        private void createLookUpTable(LookupSwitchInstruction instruction) {
            lookUpSwitch = instruction.cases().stream().map(i ->
                    new Integer[]{branchNumber, codeModel.labelToBci(i.target())}
            ).collect(Collectors.toList());
        }

        private void saveBranchConstant(BranchInstruction instruction, Long code) {
            if (Set.of(Opcode.IFEQ, Opcode.IFGE, Opcode.IFGT, Opcode.IFLE, Opcode.IFLT, Opcode.IFNE).contains(instruction.opcode())) {
                if (second instanceof LoadInstruction) {
                    BranchCoverageTracker.branchConstants.put(code, List.of(0));
                }
            } else if (first instanceof ConstantInstruction f) {
                BranchCoverageTracker.branchConstants.put(code, List.of(f.constantValue()));
            } else if (second instanceof ConstantInstruction s) {
                BranchCoverageTracker.branchConstants.put(code, List.of(s.constantValue()));
            }
        }
    }
}
