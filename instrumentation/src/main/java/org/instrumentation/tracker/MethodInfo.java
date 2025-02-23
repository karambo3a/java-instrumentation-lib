package org.instrumentation.tracker;

public class MethodInfo {
    String className;
    String methodName;
    String methodType;

    public MethodInfo(String className, String methodName, String methodType) {
        this.className = className;
        this.methodName = methodName;
        this.methodType = methodType;
    }

    @Override
    public String toString() {
        return className + '.' + methodName + methodType;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MethodInfo that)) {
            return false;
        }
        return className.equals(that.className) && methodName.equals(that.methodName) && methodType.equals(that.methodType);
    }
}
