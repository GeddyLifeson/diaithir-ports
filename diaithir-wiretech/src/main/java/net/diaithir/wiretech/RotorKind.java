package net.diaithir.wiretech;

/** The sources of torque. A dynamo next to one turns its torque into FE. */
public enum RotorKind {
    HAND_CRANK("hand_crank", "Tunwiq"),
    WINDMILL("windmill", "Aelvenhorn"),
    WATER_WHEEL("water_wheel", "Sholmarra");

    public final String id;
    public final String display;

    RotorKind(String id, String display) {
        this.id = id;
        this.display = display;
    }
}
