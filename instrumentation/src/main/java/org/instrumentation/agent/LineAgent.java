package org.instrumentation.agent;

import org.instrumentation.tracker.CoverageTracker;
import org.instrumentation.tracker.LineCoverageTracker;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.classfile.*;
import java.lang.classfile.instruction.LineNumber;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Set;

public class LineAgent {
    private static Integer classNumber = 0;

    public static void premain(String agentArgs, Instrumentation inst) {
        Set<String> classes = Set.of(agentArgs.split(","));

//         adds line coverage tracker
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classFileBuffer) {
                if (!classes.contains(className)) {
                    return classFileBuffer;
                }

                classNumber++;
                LineCoverageTracker.classes.add(className);
                LineCoverageTracker.methods.add(new ArrayList<>());
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
        private int methodNumber = 0;
        private final int classNumber;
        private final String className;

        public ClassTransformStateful(Integer classCnt, String className) {
            this.classNumber = classCnt;
            this.className = className;
        }

        @Override
        public void accept(ClassBuilder builder, ClassElement element) {
            if (element instanceof MethodModel methodModel) {
                methodNumber++;
                LineCoverageTracker.methods.get(classNumber - 1).add(this.className + '.' + methodModel.methodName().stringValue() + methodModel.methodType().stringValue());
                builder.transformMethod(methodModel, createMethodTransform());
            } else {
                builder.with(element);
            }
        }

        private MethodTransform createMethodTransform() {
            return (builder, element) -> {
                if (element instanceof CodeModel cm) {
                    builder.transformCode(cm, createCodeTransform());
                } else {
                    builder.with(element);
                }
            };
        }

        private CodeTransform createCodeTransform() {
            return (builder, element) -> {
                if (element instanceof LineNumber i) {
                    Long code = CoverageTracker.code(classNumber, methodNumber, i.line());
                    builder.ldc(code.toString())
                            .invokestatic(
                                    ClassDesc.of("org.instrumentation.tracker.LineCoverageTracker"),
                                    "logCoverage",
                                    MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)V")
                            );
                    LineCoverageTracker.logAllLine(code);
                }
                builder.with(element);
            };
        }
    }
}

