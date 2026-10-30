package net.diaithir.bosses;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;

/** The world's current Age (1..7, Rodos 8), read from diaithir-ages by reflection so the mods need not depend on each other. */
public final class Ages {
    public static final int RODOS = 8;
    private static final List<String> NUMERALS = List.of("I", "II", "III", "IV", "V", "VI", "VII");
    private static Method get;
    private static Field world, age;
    private static boolean looked;
    /** Set by tests (and by /boss age) to override what diaithir-ages says. */
    public static volatile Integer forced;

    public static int parse(String s) {
        if (s == null) return 1;
        String t = s.trim().toUpperCase(java.util.Locale.ROOT);
        if (t.equals("RODOS")) return RODOS;
        int i = NUMERALS.indexOf(t);
        if (i >= 0) return i + 1;
        try {
            return Math.clamp(Integer.parseInt(t), 1, RODOS);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public static String name(int a) {
        return a >= RODOS ? "Rodos" : NUMERALS.get(Math.clamp(a, 1, 7) - 1);
    }

    public static int current(MinecraftServer server) {
        if (forced != null) return forced;
        if (server == null || !ModList.get().isLoaded("diaithir_ages")) return 1;
        try {
            if (!looked) {
                looked = true;
                Class<?> c = Class.forName("net.diaithir.ages.mc.AgesSavedData");
                get = c.getMethod("get", MinecraftServer.class);
                world = c.getField("world");
                age = world.getType().getField("age");
            }
            if (get == null) return 1;
            return parse(String.valueOf(age.get(world.get(get.invoke(null, server)))));
        } catch (ReflectiveOperationException | RuntimeException e) {
            get = null;
            return 1;
        }
    }

    private Ages() {}
}
