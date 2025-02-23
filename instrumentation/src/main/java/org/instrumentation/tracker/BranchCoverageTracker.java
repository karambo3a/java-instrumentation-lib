package org.instrumentation.tracker;

import java.util.*;

public class BranchCoverageTracker {
    private static final Set<Long> uniqueBranchCoverage = new HashSet<>();
    private static final List<Long> notUniqueBranchCoverage = new ArrayList<>();
    private static final Set<Long> allBranch = new HashSet<>();
    private static final List<String> classes = new ArrayList<>();
    private static final List<List<MethodInfo>> methods = new ArrayList<>();
    private static boolean isUnique = false;
    private static long prev = 0;

    public static void logCoverage(long branchNumber) {
        if (isUnique) {
            uniqueBranchCoverage.add(branchNumber);
        } else {
            notUniqueBranchCoverage.add(branchNumber);
        }
    }

    public static void logAllBranch(long branchNumber) {
        allBranch.add(branchNumber);
    }

    static public void getMethodStat(MethodInfo methodInfo) {
        if (isUnique) {
            CoverageTracker.getMethodStat(methodInfo, uniqueBranchCoverage, allBranch, methods);
        } else {
            CoverageTracker.getMethodStat(methodInfo, notUniqueBranchCoverage, allBranch, methods);
        }
    }

    static public void getClassStat(String className) {
        if (isUnique) {
            CoverageTracker.getClassStat(className, uniqueBranchCoverage, allBranch, classes);
        } else {
            CoverageTracker.getClassStat(className, notUniqueBranchCoverage, allBranch, classes);
        }
    }

    static public void getStat() {
        if (isUnique) {
            CoverageTracker.getStat("", (long) uniqueBranchCoverage.size(), (long) allBranch.size());
        } else {
            CoverageTracker.getStat("", (long) notUniqueBranchCoverage.size(), (long) allBranch.size());
        }
    }

}
