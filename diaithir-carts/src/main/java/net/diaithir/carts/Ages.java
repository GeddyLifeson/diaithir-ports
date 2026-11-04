package net.diaithir.carts;

import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;

import java.util.List;

/** The server calendar Age as 0 (Telling) to 8, read from diaithir_ages by reflection (soft dependency). Without it nothing is locked. */
public final class Ages {
    private Ages() {}

    public static final int UNGATED = 99;
    private static final List<String> ORDER = List.of("TELLING", "I", "II", "III", "IV", "V", "VI", "VII", "RODOS");
    /** Used instead of the calendar when set (tests). */
    public static volatile Integer override;
    private static boolean broken;

    public static int current(MinecraftServer server) {
        Integer o = override;
        if (o != null) return o;
        if (broken || !ModList.get().isLoaded("diaithir_ages")) return UNGATED;
        try {
            Class<?> saved = Class.forName("net.diaithir.ages.mc.AgesSavedData");
            Object data = saved.getMethod("get", MinecraftServer.class).invoke(null, server);
            Object world = saved.getField("world").get(data);
            String age = (String) world.getClass().getField("age").get(world);
            int i = ORDER.indexOf(age.toUpperCase());
            return i < 0 ? UNGATED : i;
        } catch (Throwable t) {
            broken = true;
            return UNGATED;
        }
    }
}
