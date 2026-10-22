package net.diaithir.bestiary;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

/**
 * The world's current Age as a number (I = 1 ... VII = 7, Rodos = 8), read from diaithir-ages' saved story
 * (AgesSavedData.get(server).world.age) by reflection, so this mod loads without it. Without diaithir-ages the Age is I.
 */
final class Ages {
    static final int RODOS = 8;
    private static final List<String> NUMERALS = List.of("I", "II", "III", "IV", "V", "VI", "VII");
    static final Codec<Integer> CODEC = Codec.withAlternative(Codec.intRange(1, RODOS),
            Codec.STRING.comapFlatMap(s -> {
                int age = parse(s);
                return age > 0 ? DataResult.success(age) : DataResult.error(() -> "Not an Age: " + s);
            }, Ages::name));
    private static Method get;
    private static Field world, age;
    private static boolean looked;

    private Ages() {}

    static int parse(String s) {
        String t = s.trim().toUpperCase(Locale.ROOT);
        if (t.equals("RODOS")) return RODOS;
        int i = NUMERALS.indexOf(t);
        if (i >= 0) return i + 1;
        try {
            int n = Integer.parseInt(t);
            return n >= 1 && n <= RODOS ? n : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static String name(int age) {
        return age == RODOS ? "Rodos" : NUMERALS.get(age - 1);
    }

    /** True once the current Age has been looked up through diaithir-ages. */
    static boolean linked() {
        return get != null;
    }

    static int current(MinecraftServer server) {
        if (!looked) {
            looked = true;
            if (ModList.get().isLoaded("diaithir_ages")) {
                try {
                    Class<?> data = Class.forName("net.diaithir.ages.mc.AgesSavedData");
                    get = data.getMethod("get", MinecraftServer.class);
                    world = data.getField("world");
                    age = world.getType().getField("age");
                } catch (ReflectiveOperationException e) {
                    get = null;
                }
            }
        }
        if (get == null) return 1;
        try {
            int n = parse(String.valueOf(age.get(world.get(get.invoke(null, server)))));
            return n > 0 ? n : 1;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return 1;
        }
    }
}
