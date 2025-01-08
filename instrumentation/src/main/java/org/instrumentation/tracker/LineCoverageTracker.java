package org.instrumentation.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class LineCoverageTracker {
    public static final Set<Long> lineCoverage = new TreeSet<>();
    public static final Set<Long> allLine = new TreeSet<>();
    public static final List<String> classes = new ArrayList<>();
    public static final List<List<String>> methods = new ArrayList<>();

    public static void logCoverage(String lineCode) {
        lineCoverage.add(Long.valueOf(lineCode));
    }

    public static void logAllLine(Long lineCode) {
        allLine.add(lineCode);
    }

    static public void getMethodStat(String methodName) {
        CoverageTracker.getMethodStat(methodName, lineCoverage, allLine, methods);
    }

    static public void getClassStat(String className) {
        CoverageTracker.getClassStat(className, lineCoverage, allLine, classes);
    }

}
