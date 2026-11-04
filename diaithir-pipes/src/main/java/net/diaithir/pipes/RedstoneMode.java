package net.diaithir.pipes;

/** When an extract face may run. */
public enum RedstoneMode {
    ALWAYS, POWERED, UNPOWERED;

    public boolean allows(boolean powered) {
        return switch (this) {
            case ALWAYS -> true;
            case POWERED -> powered;
            case UNPOWERED -> !powered;
        };
    }

    public RedstoneMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static RedstoneMode byOrdinal(int i) {
        return i < 0 || i >= values().length ? ALWAYS : values()[i];
    }
}
