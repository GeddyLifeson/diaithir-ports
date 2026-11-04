package net.diaithir.pipes;

/** The three things a pipe can carry. */
public enum PipeKind {
    ITEM("item"), FLUID("fluid"), ENERGY("energy");

    private final String id;

    PipeKind(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
