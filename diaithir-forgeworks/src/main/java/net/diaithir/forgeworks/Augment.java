package net.diaithir.forgeworks;

/** The three augment kinds. One kind at a time sits in a machine's augment slot, and the stack size is the strength. */
public enum Augment {
    SPEED("speed_augment", "Veloqith"),
    EFFICIENCY("efficiency_augment", "Thrimvael"),
    CHANCE("chance_augment", "Ostravine");

    public final String id;
    public final String display;

    Augment(String id, String display) {
        this.id = id;
        this.display = display;
    }
}
