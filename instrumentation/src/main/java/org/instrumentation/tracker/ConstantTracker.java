package org.instrumentation.tracker;

import java.lang.constant.ConstantDesc;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantTracker {
    public static final Map<Long, List<ConstantDesc>> branchConstants = new HashMap<>();
}
