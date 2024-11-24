package org.example;

import java.util.ArrayList;
import java.util.List;

public class Example {

    private final int const1 = 3;
    public String const2;
    public static List<Integer> list = new ArrayList<>();

    public static int getNumber1(int a, Long b) {
        if (a > b) {
            return 1;
        } else if (a < b) {
            return -1;
        } else {
            return 0;
        }
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

    public static void ifStatement(int a) {
        if (a > 10) {
            list.add(a);
        }
    }

    public static void ifELseStatement(int a) {
        if (a > 10) {
            list.add(a);
        } else if (a < 0) {
            list.add(a);
        } else if (a == 5) {
            list.add(a);
        }
    }

    public static int switchStatement(int a) {
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

    public static void forLoop(int a) {
        for (int i = a; i < 10; ++i) {
            list.add(i);
        }

        for (int i = a; i > 10; --i) {
            list.add(i);
        }
    }

    public static void whileLoop(int a) {
        while (a < 10) {
            list.add(a);
            a++;
        }

        while (a > 10) {
            list.add(a);
            a--;
        }
    }

    public static void doWhileLoop(int a) {
        do {
            list.add(a);
            a++;
        } while (a < 10);

        do {
            list.add(a);
            a--;
        } while (a > 10);
    }

    public static void main(String[] args) {
        System.out.println("---");
        System.out.println("Main output\n");
        System.out.println(getNumber1(1, 2L));
        System.out.println(getString());
        voidMethod();
        ifStatement(10);
        ifELseStatement(-1);
        System.out.println(switchStatement(10));
        forLoop(11);
        whileLoop(11);
        doWhileLoop(9);

        System.out.println("---");
        System.out.println("Coverage\n");
        CoverageTracker.coverage.forEach(System.out::println);
    }
}

