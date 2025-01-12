package org.instrumentation.tracker;

import java.lang.constant.ConstantDesc;
import java.util.*;

public class BranchCoverageTracker {
    public static final Set<Long> uniqueBranchCoverage = new TreeSet<>();
    public static final List<Long> notUniqueBranchCoverage = new ArrayList<>();
    public static final Set<Long> allBranch = new TreeSet<>();
    public static final Map<Long, List<ConstantDesc>> branchConstants = new HashMap<>();
    public static final List<String> classes = new ArrayList<>();
    public static final List<List<String>> methods = new ArrayList<>();
    public static boolean isUnique = false;

    public static void logCoverage(String branchNumber) {
        if (isUnique) {
            uniqueBranchCoverage.add(Long.valueOf(branchNumber));
        } else {
            notUniqueBranchCoverage.add(Long.valueOf(branchNumber));
        }
    }

    public static void logAllBranch(Long branchNumber) {
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

}
