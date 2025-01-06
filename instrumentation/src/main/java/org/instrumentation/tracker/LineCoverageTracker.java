package org.instrumentation.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class LineCoverageTracker {
    public static final Set<Long> lineCoverage = new TreeSet<>();
    public static final Set<Long> allLine = new TreeSet<>();
    public static final List<String> classes = new ArrayList<>();
    public static final List<List<String>> methods = new ArrayList<>();

    private static final int CLASS_BITS = 21;
    private static final int METHOD_BITS = 21;
    private static final int BRANCH_BITS = 21;
    private static final long classMax = (1 << CLASS_BITS) - 1;
    private static final long methodsMax = (1 << METHOD_BITS) - 1;
    private static final long branchMax = (1 << BRANCH_BITS) - 1;

    public static Long codeLine(Integer classCnt, Integer methodCnt, Integer lineCnt) {
        return ((classCnt & classMax) |
                ((methodCnt & methodsMax) << (CLASS_BITS)) |
                ((lineCnt & branchMax) << (CLASS_BITS + METHOD_BITS)));
    }

    public static Integer[] uncodeLine(Long code) {
        long lineNumber = ((code >> (CLASS_BITS + METHOD_BITS)) & branchMax);
        long methodNumber = ((code >> CLASS_BITS) & methodsMax);
        long classNumber = (code & classMax);
        return new Integer[]{(int) classNumber, (int) methodNumber, (int) lineNumber};
    }

    public static void logCoverage(String lineCode) {
        lineCoverage.add(Long.valueOf(lineCode));
    }

    public static void logAllLine(Long lineCode) {
        allLine.add(lineCode);
    }

    static public void getMethodStat(String methodName) {
        long visitedLines = 0;
        for (var branch : lineCoverage) {
            var data = uncodeLine(branch);
            if (methods.get(data[0] - 1).get(data[1] - 1).contains(methodName)) {
                ++visitedLines;
            }
        }
        long allLines = 0;
        for (var line : allLine) {
            var data = uncodeLine(line);
            if (methods.get(data[0] - 1).get(data[1] - 1).contains(methodName)) {
                ++allLines;
            }
        }
        System.out.println(STR."===\{methodName}===");
        System.out.println(STR."Visited lines: \{visitedLines}");
        System.out.println(STR."All lines: \{allLines}");
        System.out.println(STR."\{String.format("%.4f", (double) visitedLines / allLines * 100)}%");
        System.out.println("===");
        System.out.println();
    }

    static public void getClassStat(String className) {
        long visitedLines = 0;
        for (var branch : lineCoverage) {
            var data = uncodeLine(branch);
            if (classes.get(data[0] - 1).equals(className)) {
                ++visitedLines;
            }
        }
        System.out.println("===");
        System.out.println(STR."Visited lines: \{visitedLines}");
        System.out.println(STR."All lines: \{allLine.size()}");
        System.out.println(STR."\{String.format("%.4f", (double) visitedLines / allLine.size() * 100)}%");
        System.out.println("===");
        System.out.println();
    }

}
