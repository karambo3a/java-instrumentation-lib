package org.instrumentation.tracker;

import java.lang.constant.ConstantDesc;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantTracker {
    private static final Map<Long, List<ConstantDesc>> branchConstants = new HashMap<>();

    public static void addBranchConstants(long code, List<ConstantDesc> constants) {
        branchConstants.put(code, constants);
    }

    public static Map<Long, List<ConstantDesc>> getBranchConstants() {
        return branchConstants;
    }
}
