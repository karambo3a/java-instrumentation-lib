package org.instrumentation.agent;

import java.util.function.Function;

public class AgentFactory implements Function<String, Agent> {
    public Agent apply(String type) {
        switch (type) {
            case "branch" -> {
                return new BranchAgent();
            }
            case "constant" -> {
                return new ConstantAgent();
            }
            case "indices" -> {
                return new IndicesAgent();
            }
            case "line" -> {
                return new LineAgent();
            }
            default -> throw new IllegalArgumentException("Unknown agent type");
        }
    }
}
