package org.instrumentation.tracker;

import java.util.Set;
import java.util.TreeSet;

public class LineCoverageTracker {
    public static final Set<String> lineCoverage = new TreeSet<>();
    public static final Set<String> allLine = new TreeSet<>();

    public static void logCoverage(String methodSignature, String lineNumber) {
//        lineCoverage.add(STR."\{methodSignature}: line=\{lineNumber}");
        lineCoverage.add(String.format("%s: line=%s", methodSignature, lineNumber));
    }

    static public void getMethodStat(String methodName) {
//        TODO
    }

    static public void getClassStat() {
//        TODO
    }

}
