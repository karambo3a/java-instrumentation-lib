package org.instrumentation.tracker;

import java.util.*;

public class LineCoverageTracker {
    private static final Set<Long> uniqueLineCoverage = new HashSet<>();
    private static final List<Long> notUniqueLineCoverage = new ArrayList<>();
    private static final Set<Long> allLine = new HashSet<>();
    private static final List<String> classes = new ArrayList<>();
    private static final List<List<MethodInfo>> methods = new ArrayList<>();
    private static boolean isUnique = false;
    private static long prev = 0;

    public static void logCoverage(long lineCode) {
        if (!uniqueLineCoverage.contains(lineCode)) {
            if (isUnique) {
                uniqueLineCoverage.add(lineCode);
            } else {
                notUniqueLineCoverage.add(lineCode);
            }
        }
    }

    public static void logAllLine(long lineCode) {
        allLine.add(lineCode);
    }

    static public void getMethodStat(MethodInfo methodInfo) {
        if (isUnique) {
            CoverageTracker.getMethodStat(methodInfo, uniqueLineCoverage, allLine, methods);
        } else {
            CoverageTracker.getMethodStat(methodInfo, notUniqueLineCoverage, allLine, methods);
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
