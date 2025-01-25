package org.instrumentation.tracker;

import java.lang.constant.ConstantDesc;
import java.util.*;

public class BranchCoverageTracker {
    public static final Set<Long> uniqueBranchCoverage = new HashSet<>();
    public static final List<Long> notUniqueBranchCoverage = new ArrayList<>();
    public static final Set<Long> allBranch = new HashSet<>();
    public static final Map<Long, List<ConstantDesc>> branchConstants = new HashMap<>();
    public static final List<String> classes = new ArrayList<>();
    public static final List<List<String>> methods = new ArrayList<>();
    public static boolean isUnique = false;

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

    static public void getMethodStat(String methodName) {
        if (isUnique) {
            CoverageTracker.getMethodStat(methodName, uniqueBranchCoverage, allBranch, methods);
        } else {
            CoverageTracker.getMethodStat(methodName, notUniqueBranchCoverage, allBranch, methods);
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
