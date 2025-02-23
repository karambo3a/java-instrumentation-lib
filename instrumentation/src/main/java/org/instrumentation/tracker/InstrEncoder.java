package org.instrumentation.tracker;

public class InstrEncoder {
    private static final long CLASS_BITS = 21;
    private static final long METHOD_BITS = 21;
    private static final long INSTRUCTION_BITS = 21;
    private static final long classMax = (1L << CLASS_BITS) - 1;
    private static final long methodsMax = (1L << METHOD_BITS) - 1;
    private static final long instructionMax = (1L << INSTRUCTION_BITS) - 1;

    public static long encode(long classNumber, long methodNumber, long instrNumber) {
        return ((classNumber & classMax) |
                ((methodNumber & methodsMax) << (CLASS_BITS)) |
                ((instrNumber & instructionMax) << (CLASS_BITS + METHOD_BITS)));
    }

    public static long[] decode(Long code) {
        return new long[]{
                (code & classMax),
                ((code >> CLASS_BITS) & methodsMax),
                ((code >> (CLASS_BITS + METHOD_BITS)) & instructionMax),
        };
    }
}
