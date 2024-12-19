package org.instrumentation.agent;

import org.instrumentation.tracker.BranchCoverageTracker;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.ClassFile;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.MethodModel;
import java.lang.classfile.CodeElement;
import java.lang.classfile.MethodElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.LookupSwitchInstruction;
import java.lang.classfile.instruction.TableSwitchInstruction;
import java.lang.classfile.Instruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BranchAgent {
    private static Integer classCnt = 0;

    public static void premain(String agentArgs, Instrumentation inst) {

//      adds branch coverage tracker
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classFileBuffer) {
                if (!className.startsWith("org/example/Example")) {                                         //TODO
                    return classFileBuffer;
                }
                classCnt++;
                ClassTransform classTransform = new ClassTransformStateful(classCnt);

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
        private final int classCnt;
        private int methodCnt = 0;

        public ClassTransformStateful(Integer classCnt) {
            this.classCnt = classCnt;
        }

        @Override
        public void accept(ClassBuilder builder, ClassElement element) {
            if (element instanceof MethodModel methodModel) {
                ++methodCnt;
                builder.transformMethod(methodModel, new MethodTransformStateful(methodModel, methodCnt, classCnt));
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
                builder.transformCode(codeModel, new CodeTransformStateful(methodModel, (CodeAttribute) codeModel, methodCnt, classCnt));
            } else {
                builder.with(element);
            }
        }
    }

    private static class CodeTransformStateful implements CodeTransform {
        private final MethodModel methodModel;
        private final CodeAttribute codeModel;
        private final Integer methodCnt;
        private final Integer classCnt;
        private List<Integer[]> tableSwitch = new ArrayList<>();
        private List<Integer[]> lookUpSwitch = new ArrayList<>();
        private int branchCnt = 0;
        private int lineNumber = 0;


        public CodeTransformStateful(MethodModel methodModel, CodeAttribute codeModel, Integer methodCnt, Integer classCnt) {
            this.methodModel = methodModel;
            this.codeModel = codeModel;
            this.methodCnt = methodCnt;
            this.classCnt = classCnt;
        }


        @Override
        public void accept(CodeBuilder builder, CodeElement element) {
            if (element instanceof TableSwitchInstruction instruction) {
                ++branchCnt;
                createTableSwitch(instruction);
                String str = STR."\{String.valueOf(classCnt)} \{String.valueOf(methodCnt)} \{String.valueOf(branchCnt)}";
                BranchCoverageTracker.logAllBranch(methodModel.methodName().stringValue() + methodModel.methodType().stringValue(), str);
            }
            if (element instanceof LookupSwitchInstruction instruction) {
                ++branchCnt;
                createLookUpTable(instruction);
                String str = STR."\{String.valueOf(classCnt)} \{String.valueOf(methodCnt)} \{String.valueOf(branchCnt)}";
                BranchCoverageTracker.logAllBranch(methodModel.methodName().stringValue() + methodModel.methodType().stringValue(), str);
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
                    String str = STR."\{String.valueOf(classCnt)} \{String.valueOf(methodCnt)} \{String.valueOf(lines[0])}";
                    builder.ldc(methodModel.methodName().stringValue() + methodModel.methodType().stringValue())
                            .ldc(str)
                            .invokestatic(
                                    ClassDesc.of("org.instrumentation.tracker.BranchCoverageTracker"),
                                    "logCoverage",
                                    MethodTypeDesc.ofDescriptor("(Ljava/lang/String;Ljava/lang/String;)V")
                            );
                }
            }

            if (element instanceof BranchInstruction) {
                ++branchCnt;
                String str = STR."\{String.valueOf(classCnt)} \{String.valueOf(methodCnt)} \{String.valueOf(branchCnt)}";
                builder.ldc(methodModel.methodName().stringValue() + methodModel.methodType().stringValue())
                        .ldc(str)
                        .invokestatic(
                                ClassDesc.of("org.instrumentation.tracker.BranchCoverageTracker"),
                                "logCoverage",
                                MethodTypeDesc.ofDescriptor("(Ljava/lang/String;Ljava/lang/String;)V")
                        );
                BranchCoverageTracker.logAllBranch(methodModel.methodName().stringValue() + methodModel.methodType().stringValue(), str);
            }

            if (element instanceof Instruction elem) {
                lineNumber += elem.sizeInBytes();
            }
        }

        private void createTableSwitch(TableSwitchInstruction instruction) {
            tableSwitch = instruction.cases().stream().map(i ->
                    new Integer[]{branchCnt, codeModel.labelToBci(i.target())}
            ).collect(Collectors.toList());
        }

        private void createLookUpTable(LookupSwitchInstruction instruction) {
            var a = instruction.cases().getFirst();
            lookUpSwitch = instruction.cases().stream().map(i ->
                    new Integer[]{branchCnt, codeModel.labelToBci(i.target())}
            ).collect(Collectors.toList());
        }
    }
}
