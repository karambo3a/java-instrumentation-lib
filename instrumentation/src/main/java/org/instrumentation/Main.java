package org.instrumentation;

import org.instrumentation.agent.Agent;
import org.instrumentation.agent.AgentFactory;
import org.instrumentation.tracker.BranchCoverageTracker;
import org.instrumentation.tracker.LineCoverageTracker;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.classfile.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.regex.Pattern;

public class Main {
    public static void premain(String agentArgs, Instrumentation inst) {
        if (agentArgs == null) {
            throw new IllegalArgumentException("Expected not null");
        }
        var args = List.of(agentArgs.split(","));
        if (args.size() < 2) {
            throw new IllegalArgumentException("Expected not less than 3 args");
        }
        if (args.getFirst().equals("isUnique=true")) {
            BranchCoverageTracker.setIsUnique(true);
            LineCoverageTracker.setIsUnique(true);
        } else if (args.getFirst().equals("isUnique=false")) {
            BranchCoverageTracker.setIsUnique(false);
            LineCoverageTracker.setIsUnique(false);
        } else {
            throw new IllegalArgumentException("Expected isUnique arg");
        }
        var pattern = Pattern.compile(args.get(1));

        var agentTypes = args.subList(2, args.size());
        for (var agentType : agentTypes) {
            inst.addTransformer(new Transformer(pattern, agentType));
        }
    }

    private static class Transformer implements ClassFileTransformer {
        private static final String outputFilename = "Example.class";
        private final Pattern pattern;
        private final String agentType;
        private int classNumber = 0;

        public Transformer(Pattern pattern, String agentType) {
            this.pattern = pattern;
            this.agentType = agentType;
        }

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classFileBuffer) {
            if (!pattern.matcher(className).matches()) {
                return classFileBuffer;
            }

            Agent agent = new AgentFactory().apply(agentType);
            ClassTransform classTransform = agent.createClassTransform(className, classNumber++);

            var oldClassFile = ClassFile.of().parse(classFileBuffer);
            var newClassFile = ClassFile.of().transform(oldClassFile, classTransform);
            saveClassFile(newClassFile);
            return newClassFile;
        }

        private void saveClassFile(byte[] classFile) {
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFilename))) {
                outputStream.write(classFile);
                outputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
