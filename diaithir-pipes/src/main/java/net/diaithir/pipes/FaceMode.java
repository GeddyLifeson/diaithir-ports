package net.diaithir.pipes;

/** What one face of a pipe does. */
public enum FaceMode {
    INSERT, EXTRACT, BOTH, DISCONNECTED;

    public boolean extracts() {
        return this == EXTRACT || this == BOTH;
    }

    public boolean inserts() {
        return this == INSERT || this == BOTH;
    }

    public FaceMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static FaceMode byOrdinal(int i) {
        return i < 0 || i >= values().length ? BOTH : values()[i];
    }
}
