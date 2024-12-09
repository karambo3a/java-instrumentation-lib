package org.instrumentation.tracker;

import java.util.Set;
import java.util.TreeSet;

public class BranchCoverageTracker {
    public static final Set<String> branchCoverage = new TreeSet<>();
    public static final Set<String> allBranch = new TreeSet<>();

    public static void logCoverage(String methodSignature, String lineNumber) {
        branchCoverage.add(String.format("%s: %s", methodSignature, lineNumber));
    }

    public static void logAllBranch(String methodSignature, String lineNumber) {
        allBranch.add(String.format("%s: %s", methodSignature, lineNumber));
    }

    static public void getMethodStat(String methodName) {
        long visitedBranches = branchCoverage.stream().filter(line -> line.contains(methodName)).count();
        long allBranches = allBranch.stream().filter(line -> line.contains(methodName)).count();
        System.out.println("===" + methodName + "===");
        System.out.println("Visited branches: " + visitedBranches);
        System.out.println("All branches: " + allBranches);
        System.out.println(String.format("%.4f", (double)visitedBranches / allBranches * 100) + "%");
        System.out.println("===");
        System.out.println();
    }

    static public void getClassStat() {
        System.out.println("===");
        System.out.println("Visited branches: " + branchCoverage.size());
        System.out.println("All branches: " + allBranch.size());
        System.out.println(String.format("%.4f", (double)branchCoverage.size() / allBranch.size() * 100) + "%");
        System.out.println("===");
        System.out.println();
    }
}
