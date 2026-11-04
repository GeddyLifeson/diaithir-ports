package net.diaithir.wiretech.circuit;

/** Every kind of circuit block. Combinators compute, the reader senses a neighbouring block, gate lamp and speaker act on a condition. */
public enum CircuitKind {
    CONSTANT("constant_combinator", "Constant Combinator", false, true, "const"),
    ARITHMETIC("arithmetic_combinator", "Arithmetic Combinator", true, true, "arith a + 0 -> a"),
    DECIDER("decider_combinator", "Decider Combinator", true, true, "decide a > 0 -> a one"),
    SELECTOR("selector_combinator", "Selector Combinator", true, true, "select max 0"),
    READER("circuit_reader", "Circuit Reader", false, true, null),
    GATE("circuit_gate", "Circuit Gate", true, false, "when a > 0"),
    LAMP("circuit_lamp", "Circuit Lamp", true, false, "when a > 0"),
    SPEAKER("circuit_speaker", "Circuit Speaker", true, false, "when a > 0");

    public final String id;
    public final String display;
    public final boolean hasInput;
    public final boolean hasOutput;
    public final String defaultProgram;

    CircuitKind(String id, String display, boolean hasInput, boolean hasOutput, String defaultProgram) {
        this.id = id;
        this.display = display;
        this.hasInput = hasInput;
        this.hasOutput = hasOutput;
        this.defaultProgram = defaultProgram;
    }

    /** Gate, lamp and speaker act on the world instead of writing signals. */
    public boolean acts() {
        return this == GATE || this == LAMP || this == SPEAKER;
    }
}
