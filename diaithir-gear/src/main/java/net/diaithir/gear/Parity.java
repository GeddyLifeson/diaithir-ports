package net.diaithir.gear;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;

/** Point Blank bullet damage parity against the Musket Mod. Table: data/diaithirgear/bullet_parity.json (override config/diaithirgear/bullet_parity.json). */
public final class Parity {
    private static final Map<Identifier, Float> MULT = new HashMap<>();

    private Parity() {}

    /** Test hook: treat the next hits as bullets. */
    static volatile boolean forceBullet;

    /** True when the current call stack passes through Point Blank (its hitscan calls hurt directly), so gun-in-hand melee is excluded. */
    static boolean fromPointBlank() {
        if (forceBullet) return true;
        return StackWalker.getInstance().walk(s -> s.anyMatch(f -> f.getClassName().startsWith("com.vicmatskiv.pointblank.")));
    }

    public static Map<Identifier, Float> table() { return MULT; }

    public static void load() {
        MULT.clear();
        try {
            Gson g = new Gson();
            Path ov = FMLPaths.CONFIGDIR.get().resolve("diaithirgear/bullet_parity.json");
            JsonObject o;
            if (Files.exists(ov)) {
                try (Reader r = Files.newBufferedReader(ov)) { o = g.fromJson(r, JsonObject.class); }
            } else {
                try (Reader r = new InputStreamReader(Parity.class.getResourceAsStream("/data/diaithirgear/bullet_parity.json"))) { o = g.fromJson(r, JsonObject.class); }
            }
            for (var e : o.getAsJsonObject("guns").entrySet()) {
                MULT.put(Identifier.parse(e.getKey()), e.getValue().getAsJsonObject().get("mult").getAsFloat());
            }
        } catch (Exception ex) {
            throw new IllegalStateException("diaithirgear bullet_parity", ex);
        }
    }

    /** Damage after parity for a hit by attacker's held gun; unchanged when it is not a tabled Point Blank gun. */
    public static float scaled(DamageSource src, float amount) {
        if (!GearConfig.PARITY_ON.getAsBoolean() || !(src.getEntity() instanceof LivingEntity a) || src.getDirectEntity() != a) return amount;
        ItemStack s = a.getMainHandItem();
        if (s.isEmpty()) return amount;
        Float m = MULT.get(BuiltInRegistries.ITEM.getKey(s.getItem()));
        if (m == null || !fromPointBlank()) return amount;
        double k = GearConfig.PARITY_STRENGTH.getAsDouble();
        return (float) (amount * (1.0 + (m - 1.0) * k));
    }
}
