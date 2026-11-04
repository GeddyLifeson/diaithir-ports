package net.diaithir.storagenet.quickstack;

/** Fixed defaults. Terrastorage exposed these as per-player options; here the server always has everything on. */
public final class QuickStackConfig {
    public static final int RANGE = 8;
    public static final boolean LINE_OF_SIGHT = true;
    public static final boolean HOTBAR_PROTECTION = true;
    public static final boolean SMART_DEPOSIT = true;
    public static final SortType SORT = SortType.ITEM_GROUP;
    public static final int COOLDOWN_TICKS = 4;

    private QuickStackConfig() {}
}
