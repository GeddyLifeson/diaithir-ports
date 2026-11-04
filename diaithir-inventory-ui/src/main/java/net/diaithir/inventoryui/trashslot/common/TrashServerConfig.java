package net.diaithir.inventoryui.trashslot.common;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TrashServerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ALLOW_TRASHING;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ALLOW_TRASHING = b.comment("Allow players to trash items via the trash slot").define("allowTrashing", true);
        SPEC = b.build();
    }

    private TrashServerConfig() {}
}
