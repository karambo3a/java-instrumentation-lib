package org.instrumentation.agent;


import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.MethodModel;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.attribute.CodeAttribute;

public interface Agent {
    ClassTransform createClassTransform(String className, long classNumber);

    MethodTransform createMethodTransform(String className, long classNumber, MethodModel methodModel, long methodNumber);

    CodeTransform createCodeTransform(String className, long classNumber, MethodModel methodModel, long methodNumber, CodeAttribute codeAttribute);
}
