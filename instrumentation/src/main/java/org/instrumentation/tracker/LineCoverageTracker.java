package org.instrumentation.tracker;

import java.util.Set;
import java.util.TreeSet;

public class LineCoverageTracker {
    public static final Set<String> lineCoverage = new TreeSet<>();
    public static final Set<String> allLine = new TreeSet<>();

    public static void logCoverage(String methodSignature, String lineNumber) {
        lineCoverage.add(String.format("%s: line=%s", methodSignature, lineNumber));
    }

    public static void logAllLine(String methodSignature, String lineNumber) {
        allLine.add(String.format("%s: line=%s", methodSignature, lineNumber));
    }

    static public void getMethodStat(String methodName) {
        long visitedLines = lineCoverage.stream().filter(line -> line.contains(methodName)).count();
        long allLines = allLine.stream().filter(line -> line.contains(methodName)).count();
        System.out.println("===" + methodName + "===");
        System.out.println("Visited lines: " + visitedLines);
        System.out.println("All lines: " + allLines);
        System.out.println(String.format("%.4f", (double)visitedLines / allLines * 100) + "%");
        System.out.println("===");
        System.out.println();
    }

    static public void getClassStat() {
        System.out.println("===");
        System.out.println("Visited lines: " + lineCoverage.size());
        System.out.println("All lines: " + allLine.size());
        System.out.println(String.format("%.4f", (double)lineCoverage.size() / allLine.size() * 100) + "%");
        System.out.println("===");
        System.out.println();
    }

}
