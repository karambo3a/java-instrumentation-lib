package org.instrumentation.tracker;

import java.lang.constant.ConstantDesc;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndicesTracker {
    private static final Map<Long, List<ConstantDesc>> arrayIndices = new HashMap<>();

    public static void addArrayIndices(long code, List<ConstantDesc> indices) {
        arrayIndices.put(code, indices);
    }

    public static Map<Long, List<ConstantDesc>> getArrayIndices() {
        return arrayIndices;
    }
}
