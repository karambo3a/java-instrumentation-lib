package org.instrumentation.tracker;

import java.util.List;

public interface CoverageTracker {
    int CLASS_BITS = 21;
    int METHOD_BITS = 21;
    int INSTRUCTION_BITS = 21;
    long classMax = (1 << CLASS_BITS) - 1;
    long methodsMax = (1 << METHOD_BITS) - 1;
    long instructionMax = (1 << INSTRUCTION_BITS) - 1;

    static Long code(Integer classCnt, Integer methodCnt, Integer branchCnt) {
        return ((classCnt & classMax) |
                ((methodCnt & methodsMax) << (CLASS_BITS)) |
                ((branchCnt & instructionMax) << (CLASS_BITS + METHOD_BITS)));
    }

    static Integer[] uncode(Long code) {
        long branchNumber = ((code >> (CLASS_BITS + METHOD_BITS)) & instructionMax);
        long methodNumber = ((code >> CLASS_BITS) & methodsMax);
        long classNumber = (code & classMax);
        return new Integer[]{(int) classNumber, (int) methodNumber, (int) branchNumber};
    }

    static void getMethodStat(String methodName, Iterable<Long> coverage, Iterable<Long> allCoverage, List<List<String>> methods) {
        long visited = 0;
        for (var cov : coverage) {
            var data = uncode(cov);
            if (methods.get(data[0] - 1).get(data[1] - 1).equals(methodName)) {
                ++visited;
            }
        }
        long all = 0;
        for (var cov : allCoverage) {
            var data = uncode(cov);
            if (methods.get(data[0] - 1).get(data[1] - 1).equals(methodName)) {
                ++all;
            }
        }
        printStat(methodName, visited, all);
    }

    static void getClassStat(String className, Iterable<Long> coverage, Iterable<Long> allCoverage, List<String> classes) {
        long visited = 0;
        for (var branch : coverage) {
            var data = CoverageTracker.uncode(branch);
            if (classes.get(data[0] - 1).equals(className)) {
                ++visited;
            }
        }
        long all = 0;
        for (var cov : allCoverage) {
            var data = uncode(cov);
            if (classes.get(data[0] - 1).equals(className)) {
                ++all;
            }
        }
        printStat(className, visited, all);
    }

    private static void printStat(String name, Long visited, Long all) {
        System.out.println(STR."=== \{name} ===");
        System.out.println(STR."Visited: \{visited}");
        System.out.println(STR."All: \{all}");
        System.out.println(STR."\{String.format("%.4f", (double) visited / all * 100)}%");
        System.out.println("===");
        System.out.println();
    }

}
