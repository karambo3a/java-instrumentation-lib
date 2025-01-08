package org.instrumentation.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class BranchCoverageTracker {
    public static final Set<Long> branchCoverage = new TreeSet<>();
    public static final Set<Long> allBranch = new TreeSet<>();
    public static final List<String> classes = new ArrayList<>();
    public static final List<List<String>> methods = new ArrayList<>();

    public static void logCoverage(String branchNumber) {
        branchCoverage.add(Long.valueOf(branchNumber));
    }

    public static void logAllBranch(Long branchNumber) {
        allBranch.add(branchNumber);
    }

    static public void getMethodStat(String methodName) {
        CoverageTracker.getMethodStat(methodName, branchCoverage, allBranch, methods);
    }

    static public void getClassStat(String className) {
        CoverageTracker.getClassStat(className, branchCoverage, allBranch, classes);
    }

}
