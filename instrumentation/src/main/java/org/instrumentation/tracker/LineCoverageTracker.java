package org.instrumentation.tracker;

import java.util.*;

public class LineCoverageTracker {
    private static final Set<Long> uniqueLineCoverage = new HashSet<>();
    private static final List<Long> notUniqueLineCoverage = new ArrayList<>();
    private static final Set<Long> allLine = new HashSet<>();
    private static final List<String> classes = new ArrayList<>();
    private static final List<List<MethodInfo>> methods = new ArrayList<>();
    private static boolean isUnique = false;
    private static long prev = -1;

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

    public static long countMethodMetrics(MethodInfo methodInfo, Iterable<Long> coverage, List<List<MethodInfo>> methods) {
        return CoverageTracker.countMethodMetrics(methodInfo, coverage, methods);
    }

    public static long countClassMetrics(String className, Iterable<Long> coverage, List<String> classes) {
        return CoverageTracker.countClassMetrics(className, coverage, classes);
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

    public static Set<Long> getUniqueLineCoverage() {
        return uniqueLineCoverage;
    }

    public static List<Long> getNotUniqueLineCoverage() {
        return notUniqueLineCoverage;
    }

    public static Set<Long> getAllLine() {
        return allLine;
    }

    public static List<String> getClasses() {
        return classes;
    }

    public static void addClass(String className) {
        classes.add(className);
    }

    public static List<List<MethodInfo>> getMethods() {
        return methods;
    }

    public static void addMethod(MethodInfo methodInfo) {
        methods.getLast().add(methodInfo);
    }

    public static void setIsUnique(boolean unique) {
        isUnique = unique;
    }

    public static long getPrev() {
        return prev;
    }

    public static void setPrev(long value) {
        prev = value;
    }
}
