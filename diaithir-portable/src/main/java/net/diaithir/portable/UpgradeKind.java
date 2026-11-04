package net.diaithir.portable;

/** Upgrade types. Stack multiplies the stack size; the rest act on tick or on item events. */
public enum UpgradeKind {
    STACK("stack_upgrade"),
    PICKUP("pickup_upgrade"),
    FEEDING("feeding_upgrade"),
    MAGNET("magnet_upgrade"),
    VOID("void_upgrade"),
    SMELTING("smelting_upgrade"),
    CRAFTING("crafting_upgrade"),
    JUKEBOX("jukebox_upgrade");

    public final String id;

    UpgradeKind(String id) {
        this.id = id;
    }

    public static UpgradeKind byId(String id) {
        for (UpgradeKind k : values()) if (k.id.equals(id)) return k;
        return null;
    }
}
