package org.example;

import org.instrumentation.tracker.BranchCoverageTracker;
import org.instrumentation.tracker.LineCoverageTracker;
import org.instrumentation.tracker.MethodInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.example.Example.doWhileLoop;
import static org.example.Example.forLoop;
import static org.example.Example.getNumber1;
import static org.example.Example.getString;
import static org.example.Example.ifELseStatement;
import static org.example.Example.ifStatement;
import static org.example.Example.instanceOfStatement;
import static org.example.Example.lookUpSwitch;
import static org.example.Example.nestedIfElseLoop;
import static org.example.Example.rangeForLoop;
import static org.example.Example.stringSwitchCase;
import static org.example.Example.tableSwitch;
import static org.example.Example.ternaryOperator;
import static org.example.Example.tryCatch;
import static org.example.Example.voidMethod;
import static org.example.Example.whileLoop;
import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgentTest {

    @BeforeAll
    public static void runMain() {
        System.out.println("---");
        System.out.println("Main output\n");
        System.out.println(getNumber1(1, 2L));
        System.out.println(getString());
        rangeForLoop();
        voidMethod();
        ifStatement(10);
        ifELseStatement(-1);
        System.out.println(lookUpSwitch(10));
        System.out.println(tableSwitch(20));
        forLoop(1);
        whileLoop(15);
        doWhileLoop(6);
        System.out.println(instanceOfStatement(10));
        nestedIfElseLoop(15);
        ternaryOperator(1);
        tryCatch();
        stringSwitchCase("aaa");
    }

    @Order(1)
    @Test
    public void getLineCoverage() {
        System.out.println();
        System.out.println("---");
        System.out.println("Line Coverage\n");

        LineCoverageTracker.getClassStat("org/example/Example");
        assertEquals(59L, LineCoverageTracker.countClassMetrics("org/example/Example", LineCoverageTracker.getUniqueLineCoverage(), LineCoverageTracker.getClasses()));
        assertEquals(101L, LineCoverageTracker.countClassMetrics("org/example/Example", LineCoverageTracker.getAllLine(), LineCoverageTracker.getClasses()));
    }

    @Order(2)
    @Test
    public void getLineCoverageMethodNestedIfElseLoop() {
        LineCoverageTracker.getMethodStat(new MethodInfo("org/example/Example", "nestedIfElseLoop", "(I)V"));
        assertEquals(4L, LineCoverageTracker.countMethodMetrics(new MethodInfo("org/example/Example", "nestedIfElseLoop", "(I)V"), LineCoverageTracker.getUniqueLineCoverage(), LineCoverageTracker.getMethods()));
        assertEquals(8L, LineCoverageTracker.countMethodMetrics(new MethodInfo("org/example/Example", "nestedIfElseLoop", "(I)V"), LineCoverageTracker.getAllLine(), LineCoverageTracker.getMethods()));
    }

    @Order(3)
    @Test
    public void getLineCoverageMethodOf() {
        LineCoverageTracker.getMethodStat(new MethodInfo("org/example/Example", "of", "()V"));
        assertEquals(0, LineCoverageTracker.countMethodMetrics(new MethodInfo("org/example/Example", "of", "()V"), LineCoverageTracker.getUniqueLineCoverage(), LineCoverageTracker.getMethods()));
        assertEquals(0, LineCoverageTracker.countMethodMetrics(new MethodInfo("org/example/Example", "of", "()V"), LineCoverageTracker.getAllLine(), LineCoverageTracker.getMethods()));
    }

    @Order(4)
    @Test
    public void getLineCoverageAll() {
        LineCoverageTracker.getStat();
        assertEquals(59L, LineCoverageTracker.getUniqueLineCoverage().size());
        assertEquals(101L, LineCoverageTracker.getAllLine().size());
    }

    @Order(5)
    @Test
    public void getBranchCoverage() {
        System.out.println("---");
        System.out.println("Branch Coverage\n");

        BranchCoverageTracker.getClassStat("org/example/Example");
        assertEquals(13L, BranchCoverageTracker.countClassMetrics("org/example/Example", BranchCoverageTracker.getUniqueBranchCoverage(), BranchCoverageTracker.getClasses()));
        assertEquals(27L, BranchCoverageTracker.countClassMetrics("org/example/Example", BranchCoverageTracker.getAllBranch(), BranchCoverageTracker.getClasses()));
    }

    @Order(6)
    @Test
    public void getBranchCoverageNestedIfElseLoop() {
        BranchCoverageTracker.getMethodStat(new MethodInfo("org/example/Example", "nestedIfElseLoop", "(I)V"));
        assertEquals(2L, BranchCoverageTracker.countMethodMetrics(new MethodInfo("org/example/Example", "nestedIfElseLoop", "(I)V"), BranchCoverageTracker.getUniqueBranchCoverage(), BranchCoverageTracker.getMethods()));
        assertEquals(3L, BranchCoverageTracker.countMethodMetrics(new MethodInfo("org/example/Example", "nestedIfElseLoop", "(I)V"), BranchCoverageTracker.getAllBranch(), BranchCoverageTracker.getMethods()));
    }

    @Order(7)
    @Test
    public void getBranchCoverageAll() {
        BranchCoverageTracker.getStat();
        assertEquals(13L, BranchCoverageTracker.getUniqueBranchCoverage().size());
        assertEquals(27L, BranchCoverageTracker.getAllBranch().size());
    }
}
