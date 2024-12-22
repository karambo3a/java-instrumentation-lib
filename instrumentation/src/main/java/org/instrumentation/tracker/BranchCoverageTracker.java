package org.instrumentation.tracker;

import java.util.ArrayList;
import java.util.List;

public class BranchCoverageTracker {
    public static final List<Long> branchCoverage = new ArrayList<>();
    public static final List<Long> allBranch = new ArrayList<>();
    public static final List<String> classes = new ArrayList<>();
    public static final List<List<String>> methods = new ArrayList<>();

    private static final int CLASS_BITS = 21;
    private static final int METHOD_BITS = 21;
    private static final int BRANCH_BITS = 21;
    private static final long classMax = (1 << CLASS_BITS) - 1;
    private static final long methodsMax = (1 << METHOD_BITS) - 1;
    private static final long branchMax = (1 << BRANCH_BITS) - 1;

    public static Long codeBranch(Integer classCnt, Integer methodCnt, Integer branchCnt) {
        return ((classCnt & classMax) |
                ((methodCnt & methodsMax) << (CLASS_BITS)) |
                ((branchCnt & branchMax) << (CLASS_BITS + METHOD_BITS)));
    }

    public static Integer[] uncodeBranch(Long code) {
        long branchNumber = ((code >> (CLASS_BITS + METHOD_BITS)) & branchMax);
        long methodNumber = ((code >> CLASS_BITS) & methodsMax);
        long classNumber = (code & classMax);
        return new Integer[]{(int) classNumber, (int) methodNumber, (int) branchNumber};
    }

    public static void logCoverage(String branchNumber) {
        branchCoverage.add(Long.valueOf(branchNumber));
    }

    public static void logAllBranch(Long branchNumber) {
        allBranch.add(branchNumber);
    }

    static public void getMethodStat(String methodName) {
        long visitedBranches = 0;
        for (var branch : branchCoverage) {
            var data = uncodeBranch(branch);
            if (methods.get(data[0] - 1).get(data[1] - 1).contains(methodName)) {
                ++visitedBranches;
            }
        }
        long allBranches = 0;
        for (var branch : allBranch) {
            var data = uncodeBranch(branch);
            if (methods.get(data[0] - 1).get(data[1] - 1).contains(methodName)) {
                ++allBranches;
            }
        }
        System.out.println(STR."===\{methodName}===");
        System.out.println(STR."Visited branches: \{visitedBranches}");
        System.out.println(STR."All branches: \{allBranches}");
        System.out.println(STR."\{String.format("%.4f", (double) visitedBranches / allBranches * 100)}%");
        System.out.println("===");
        System.out.println();
    }

    static public void getClassStat(String className) {
        long visitedBranches = 0;
        for (var branch : branchCoverage) {
            var data = uncodeBranch(branch);
            if (classes.get(data[0] - 1).equals(className)) {
                ++visitedBranches;
            }
        }
        System.out.println("===");
        System.out.println(STR."Visited branches: \{visitedBranches}");
        System.out.println(STR."All branches: \{allBranch.size()}");
        System.out.println(STR."\{String.format("%.4f", (double) visitedBranches / allBranch.size() * 100)}%");
        System.out.println("===");
        System.out.println();
    }
}
