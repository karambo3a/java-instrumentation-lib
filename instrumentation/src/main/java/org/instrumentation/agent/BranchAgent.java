package org.instrumentation.agent;

import org.instrumentation.tracker.BranchCoverageTracker;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.attribute.LineNumberTableAttribute;
import java.lang.classfile.instruction.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BranchAgent {
    public static void premain(String agentArgs, Instrumentation inst) {

//      adds branch coverage tracker
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classFileBuffer) {
                if (!className.equals("org/example/Example")) {
                    return classFileBuffer;
                }
                ClassTransform classTransform = (builder, element) -> {
                    if (element instanceof MethodModel methodModel) {
                        builder.transformMethod(methodModel, createMethodTransform(methodModel));
                    } else {
                        builder.with(element);
                    }
                };

                var oldClassFile = ClassFile.of().parse(classFileBuffer);
                var newClassFile = ClassFile.of().transform(oldClassFile, classTransform);
                saveClassFile(newClassFile);
                return newClassFile;
            }

            private MethodTransform createMethodTransform(MethodModel methodModel) {
                return (builder, element) -> {
                    if (element instanceof CodeModel cm) {
                        LineNumberTableAttribute lineTable = null;
                        for (var attr : cm.attributes()) {
                            if (attr instanceof LineNumberTableAttribute lineAttr) {
                                lineTable = lineAttr;
                            }
                        }
                        builder.transformCode(cm, createCodeTransform(methodModel, (CodeAttribute) cm, lineTable));
                    } else {
                        builder.with(element);
                    }
                };
            }

            private CodeTransform createCodeTransform(MethodModel methodModel, CodeAttribute codeModel, LineNumberTableAttribute table) {
                return new CodeTransformStateful(methodModel, codeModel, table);
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

    private static class CodeTransformStateful implements CodeTransform {
        private int lineNumber = 0;
        private int lineCnt = 0;
        private final MethodModel methodModel;
        private final CodeAttribute codeModel;
        private final LineNumberTableAttribute table;
        private List<Integer[]> tableSwitch = new ArrayList<>();
        private List<Integer[]> lookUpSwitch = new ArrayList<>();


        public CodeTransformStateful(MethodModel methodModel, CodeAttribute codeModel, LineNumberTableAttribute table) {
            this.methodModel = methodModel;
            this.codeModel = codeModel;
            this.table = table;
        }


        @Override
        public void accept(CodeBuilder builder, CodeElement element) {
            if (element instanceof TableSwitchInstruction instruction) {
                if (table != null) {
                    createTableSwitch(instruction);
                    String str = STR."begin: \{table.lineNumbers().get(lineCnt - 1).lineNumber()}";
                    BranchCoverageTracker.logAllBranch(methodModel.methodName().stringValue() + methodModel.methodType().stringValue(), str);
                }
            }
            if (element instanceof LookupSwitchInstruction instruction) {
                if (table != null) {
                    createLookUpTable(instruction);
                    String str = STR."begin: \{table.lineNumbers().get(lineCnt - 1).lineNumber()}";
                    BranchCoverageTracker.logAllBranch(methodModel.methodName().stringValue() + methodModel.methodType().stringValue(), str);
                }
            }

            builder.with(element);

            if (element instanceof Instruction) {
                Integer[] lines = null;
                if (tableSwitch.stream().anyMatch(l -> l[1] == lineNumber)) {
                    lines = tableSwitch.stream().filter(l -> l[1] == lineNumber).findFirst().get();
                } else if (lookUpSwitch.stream().anyMatch(l -> l[1] == lineNumber)) {
                    lines = lookUpSwitch.stream().filter(l -> l[1] == lineNumber).findFirst().get();
                }
                if (table != null && lines != null) {
                    String str = STR."begin: \{lines[0]}; branch=\{table.lineNumbers().get(lineCnt - 1).lineNumber()}";
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
                if (table != null) {
                    String str = STR."begin: \{table.lineNumbers().get(lineCnt - 1).lineNumber()}; branch=\{table.lineNumbers().get(lineCnt).lineNumber()}";
                    builder.ldc(methodModel.methodName().stringValue() + methodModel.methodType().stringValue())
                            .ldc(str)
                            .invokestatic(
                                    ClassDesc.of("org.instrumentation.tracker.BranchCoverageTracker"),
                                    "logCoverage",
                                    MethodTypeDesc.ofDescriptor("(Ljava/lang/String;Ljava/lang/String;)V")
                            );
                    BranchCoverageTracker.logAllBranch(methodModel.methodName().stringValue() + methodModel.methodType().stringValue(), STR."begin: \{table.lineNumbers().get(lineCnt - 1).lineNumber()}");
                }
            }

            if (element instanceof Instruction elem) {
                lineNumber += elem.sizeInBytes();
            }
            if (element instanceof LineNumber) {
                lineCnt += 1;
            }
        }

        private void createTableSwitch(TableSwitchInstruction instruction) {
            tableSwitch = instruction.cases().stream().map(i ->
                    new Integer[]{table.lineNumbers().get(lineCnt - 1).lineNumber(), codeModel.labelToBci(i.target())}
            ).collect(Collectors.toList());
            tableSwitch.add(new Integer[]{table.lineNumbers().get(lineCnt - 1).lineNumber(), codeModel.labelToBci(instruction.defaultTarget())});
        }

        private void createLookUpTable(LookupSwitchInstruction instruction) {
            lookUpSwitch = instruction.cases().stream().map(i ->
                    new Integer[]{table.lineNumbers().get(lineCnt - 1).lineNumber(), codeModel.labelToBci(i.target())}
            ).collect(Collectors.toList());
            lookUpSwitch.add(new Integer[]{table.lineNumbers().get(lineCnt - 1).lineNumber(), codeModel.labelToBci(instruction.defaultTarget())});
        }
    }
}
