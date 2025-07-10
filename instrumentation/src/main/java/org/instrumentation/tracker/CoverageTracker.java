package org.instrumentation.tracker;

import java.util.List;

public interface CoverageTracker {
    static long countMethodMetrics(MethodInfo methodInfo, Iterable<Long> coverage, List<List<MethodInfo>> methods) {
        long metric = 0L;
        for (var cov : coverage) {
            var data = InstrEncoder.decode(cov);
            if (methods.get((int) data[0]).get((int) data[1]).equals(methodInfo)) {
                ++metric;
            }
        }
        return metric;
    }

    static void getMethodStat(MethodInfo methodInfo, Iterable<Long> coverage, Iterable<Long> allCoverage, List<List<MethodInfo>> methods) {
        long visited = countMethodMetrics(methodInfo, coverage, methods);
        long all = countMethodMetrics(methodInfo, allCoverage, methods);
        getStat(methodInfo.toString(), visited, all);
    }

    static long countClassMetrics(String className, Iterable<Long> coverage, List<String> classes) {
        long metric = 0L;
        for (var cov : coverage) {
            var data = InstrEncoder.decode(cov);
            if (classes.get((int) data[0]).equals(className)) {
                ++metric;
            }
        }
        return metric;
    }

    static void getClassStat(String className, Iterable<Long> coverage, Iterable<Long> allCoverage, List<String> classes) {
        long visited = countClassMetrics(className, coverage, classes);
        long all = countClassMetrics(className, allCoverage, classes);
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
