package org.instrumentation.tracker;

import java.util.*;

public class LineCoverageTracker {
    public static final Set<Long> uniqueLineCoverage = new HashSet<>();
    public static final List<Long> notUniqueLineCoverage = new ArrayList<>();
    public static final Set<Long> allLine = new HashSet<>();
    public static final List<String> classes = new ArrayList<>();
    public static final List<List<String>> methods = new ArrayList<>();
    public static boolean isUnique = false;

    public static void logCoverage(long lineCode) {
        if (isUnique) {
            uniqueLineCoverage.add(lineCode);
        } else {
            notUniqueLineCoverage.add(lineCode);
        }
    }

    public static void logAllLine(long lineCode) {
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

    static public void getStat() {
        if (isUnique) {
            CoverageTracker.getStat("", (long) uniqueLineCoverage.size(), (long) allLine.size());
        } else {
            CoverageTracker.getStat("", (long) notUniqueLineCoverage.size(), (long) allLine.size());
        }
    }

}
