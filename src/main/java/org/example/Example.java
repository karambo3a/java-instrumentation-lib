package org.example;

import org.instrumentation.tracker.*;

import java.util.ArrayList;
import java.util.List;

public class Example {

    private final int const1 = 3;
    public String const2;
    public static List<Integer> list = new ArrayList<>();

    public static int getNumber1(int a, Long b) {    //// a = 1  b = 2
        if (a > b) {
            return -1;
        } else if (a < b) {  /// 1 < 2
            int f = 1;
        } else {
            return 0;
        }
        return 10;
    }

    public static int getNumber2(int a, Long b, String c) {
        return 10000000;
    }

    public static String getString() {
        return "string";
    }

    public static void voidMethod() {
        System.out.println("void method");
    }

    public static void ifStatement(int a) {   //// a = 10
        if (a > 10) {
            list.add(a);
        }
    }

    public static void ifELseStatement(int a) {   //// a = -1
        if (a > 10) {
            list.add(a);
        } else if (a < 0) {
            list.add(a);
        } else if (a == 5) {
            list.add(a);
        }
    }

    public static int lookUpSwitch(int a) {     /// a = 10
        switch (a) {
            case 10 -> {
                return 10 + 10;
            }
            case 100 -> {
                return 20 + 20;
            }
            default -> {
                return 30 + 30;
            }
        }
    }

    public static int tableSwitch(int a) {     /// a = 20
        switch (a) {
            case 10 -> {
                return 10 + 10;
            }
            case 11 -> {
                return 20 + 20;
            }
            case 12 -> {
                return 30 + 30;
            }
        }
        return 10;
    }

    public static void forLoop(int a) {    /// a = 11
        for (int i = a; i < 10; ++i) {
            list.add(i);
        }

        for (int i = a; i > 10; --i) {
            list.add(i);
        }
    }

    public static void whileLoop(int a) {   /// a = 11
        while (a < 10) {
            list.add(a);
            a++;
        }

        while (a > 10) {
            list.add(a);
            a--;
        }
    }

    public static void doWhileLoop(int a) {   ///  a = 9
        do {
            list.add(a);
            a++;
        } while (a < 10);

        do {
            list.add(a);
            a--;
        } while (a > 10);
    }

    public static boolean instanceOfStatement(Object a) {   ///  a = 9
        if (a instanceof String) {
            return true;
        }
        if (a instanceof Integer) {
            return true;
        }
        return false;
    }

    public static void nestedIfElseLoop(int a) {   ///  a = 9
        if (a > 10) {
            if (a < 20) {
                System.out.println(100);
            } else {
                System.out.println(200);
            }
        } else {
            if (a < 0) {
                System.out.println(300);
            } else {
                System.out.println(400);
            }
        }
    }

    public static void ternaryOperator(int a) {
        a = a == 0 ? 1 : 2;
    }

    public static void tryCatch() {
        try {
            throw new RuntimeException();
        } catch (Exception e) {

        }
        try {
            int a = 0;
        } catch (Exception e) {

        }
    }


    public static void main() {
        System.out.println("---");
        System.out.println("Main output\n");
        System.out.println(getNumber1(1, 2L));
        System.out.println(getString());
        voidMethod();
        ifStatement(10);
        ifELseStatement(-1);
        System.out.println(lookUpSwitch(10));
        System.out.println(tableSwitch(20));
        forLoop(11);
        whileLoop(11);
        doWhileLoop(9);
        System.out.println(instanceOfStatement(10));
        nestedIfElseLoop(15);
        ternaryOperator(1);
        tryCatch();
        Example2.fun();

        System.out.println("---");
        System.out.println("Line Coverage\n");
        LineCoverageTracker.getClassStat("org/example/Example");
        LineCoverageTracker.getMethodStat("nestedIfElseLoop");

        System.out.println("---");
        System.out.println("Branch Coverage\n");
        BranchCoverageTracker.getClassStat("org/example/Example");
        BranchCoverageTracker.getMethodStat("nestedIfElseLoop");
    }
}

