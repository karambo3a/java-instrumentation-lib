package org.instrumentation.agent;

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
import java.util.Set;

public class LineAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        Set<String> classes = Set.of(agentArgs.split(","));

        // prints method's signature
//        inst.addTransformer(new ClassFileTransformer() {
//            @Override
//            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
//                                    ProtectionDomain protectionDomain, byte[] classFileBuffer) {
//                if (!className.equals("org/example/Example")) {
//                    return classFileBuffer;
//                }
//                System.out.println("Methods\n");
//                var classModel = ClassFile.of().parse(classFileBuffer);
//                var methods = classModel.methods();
//                for (var method : methods) {
//                    String descriptor = method.methodType().stringValue();
//                    System.out.println(STR."\{method.methodName().stringValue()} \{descriptor}");
//                }
//                return classFileBuffer;
//            }
//        });

//         adds line coverage tracker
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classFileBuffer) {
                if (!classes.contains(className)) {
                    return classFileBuffer;
                }

                ClassTransform classTransform = (builder, element) -> {
                    if (element instanceof MethodModel methodModel) {
                        String methodName = methodModel.methodName().stringValue();
                        String methodDescriptor = methodModel.methodType().stringValue();
                        builder.transformMethod(methodModel, createMethodTransform(className + " " + methodName + methodDescriptor));
                    } else {
                        builder.with(element);
                    }
                };

                var oldClassFile = ClassFile.of().parse(classFileBuffer);
                var newClassFile = ClassFile.of().transform(oldClassFile, classTransform);
                saveClassFile(newClassFile);
                return newClassFile;
            }

            private MethodTransform createMethodTransform(String methodSignature) {
                return (builder, element) -> {
                    if (element instanceof CodeModel cm) {
                        builder.transformCode(cm, createCodeTransform(methodSignature));
                    } else {
                        builder.with(element);
                    }
                };
            }

            private CodeTransform createCodeTransform(String methodSignature) {
                return (builder, element) -> {
                    if (element instanceof LineNumber i) {
                        builder.ldc(methodSignature)
                                .ldc(String.valueOf(i.line()))
                                .invokestatic(
                                        ClassDesc.of("org.instrumentation.tracker.LineCoverageTracker"),
                                        "logCoverage",
                                        MethodTypeDesc.ofDescriptor("(Ljava/lang/String;Ljava/lang/String;)V")
                                );
                        LineCoverageTracker.logAllLine(methodSignature, String.valueOf(i.line()));
                    }
                    builder.with(element);
                };
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
}

