package org.instrumentation.tracker;

import java.util.List;

public interface CoverageTracker {
    static void getMethodStat(MethodInfo methodInfo, Iterable<Long> coverage, Iterable<Long> allCoverage, List<List<MethodInfo>> methods) {
        long visited = 0;
        for (var cov : coverage) {
            var data = InstrEncoder.decode(cov);
            if (methods.get((int) data[0]).get((int) data[1]).equals(methodInfo)) {
                ++visited;
            }
        }
        long all = 0;
        for (var cov : allCoverage) {
            var data = InstrEncoder.decode(cov);
            if (methods.get((int) data[0]).get((int) data[1]).equals(methodInfo)) {
                ++all;
            }
        }
        getStat(methodInfo.toString(), visited, all);
    }

    static void getClassStat(String className, Iterable<Long> coverage, Iterable<Long> allCoverage, List<String> classes) {
        long visited = 0;
        for (var branch : coverage) {
            var data = InstrEncoder.decode(branch);
            if (classes.get((int) data[0]).equals(className)) {
                ++visited;
            }
        }
        long all = 0;
        for (var cov : allCoverage) {
            var data = InstrEncoder.decode(cov);
            if (classes.get((int) data[0]).equals(className)) {
                ++all;
            }
        }
        getStat(className, visited, all);
    }

    static void getStat(String name, Long visited, Long all) {
        System.out.println(STR."=== \{name} ===");
        System.out.println(STR."Visited: \{visited}");
        System.out.println(STR."All: \{all}");
        if (all != 0) {
            System.out.println(STR."\{String.format("%.4f", (double) visited / all * 100)}%");
        } else {
            System.out.println("0.0000%");
        }
        System.out.println("===");
        System.out.println();
    }
}
