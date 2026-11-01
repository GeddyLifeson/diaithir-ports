package net.diaithir.forcefield;

/** The upgrade modules a projector takes. The id is the item path and the key saved on the projector. */
public enum Module {
    RANGE("range_module"),
    STRENGTH("strength_module"),
    MOB_FILTER("mob_filter_module"),
    GUEST_LIST("guest_list_module"),
    CAPACITOR("capacitor_module"),
    STING("sting_module");

    public final String itemId;

    Module(String itemId) {
        this.itemId = itemId;
    }
}
