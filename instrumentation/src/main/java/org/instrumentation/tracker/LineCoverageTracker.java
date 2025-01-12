package org.instrumentation.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class LineCoverageTracker {
    public static final Set<Long> uniqueLineCoverage = new TreeSet<>();
    public static final List<Long> notUniqueLineCoverage = new ArrayList<>();
    public static final Set<Long> allLine = new TreeSet<>();
    public static final List<String> classes = new ArrayList<>();
    public static final List<List<String>> methods = new ArrayList<>();
    public static boolean isUnique = false;

    public static void logCoverage(String lineCode) {
        if (isUnique) {
            uniqueLineCoverage.add(Long.valueOf(lineCode));
        } else {
            notUniqueLineCoverage.add(Long.valueOf(lineCode));
        }
    }

    public static void logAllLine(Long lineCode) {
        allLine.add(lineCode);
    }

    static public void getMethodStat(String methodName) {
        if (isUnique) {
            CoverageTracker.getMethodStat(methodName, uniqueLineCoverage, allLine, methods);
        } else {
            CoverageTracker.getMethodStat(methodName, notUniqueLineCoverage, allLine, methods);
        }
    }

    static public void getClassStat(String className) {
        if (isUnique) {
            CoverageTracker.getClassStat(className, uniqueLineCoverage, allLine, classes);
        } else {
            CoverageTracker.getClassStat(className, notUniqueLineCoverage, allLine, classes);
        }
    }

}
