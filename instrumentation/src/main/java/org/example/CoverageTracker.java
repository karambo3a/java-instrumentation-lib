package org.example;

import java.util.Set;
import java.util.TreeSet;

public class CoverageTracker {
    public static final Set<String> coverage = new TreeSet<>();

    public static void logCoverage(String methodSignature, String lineNumber) {
            coverage.add(methodSignature + ": line=" + lineNumber);
        }
}
