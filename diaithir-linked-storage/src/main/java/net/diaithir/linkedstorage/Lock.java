package net.diaithir.linkedstorage;

/** Who may open a linked block and which scope it shares in. */
public enum Lock {
    OWNER, TEAM, PUBLIC;

    public Lock next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static Lock byOrdinal(int i) {
        return i >= 0 && i < values().length ? values()[i] : OWNER;
    }
}
