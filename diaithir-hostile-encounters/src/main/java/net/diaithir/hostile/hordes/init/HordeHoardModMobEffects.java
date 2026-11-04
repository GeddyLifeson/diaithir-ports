package net.diaithir.hostile.hordes.init;

import net.diaithir.hostile.hordes.platform.Services;
import net.diaithir.hostile.hordes.potion.ThinkingMobEffect;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

public class HordeHoardModMobEffects {

    public static final Holder<MobEffect> THINKING =
            Services.PLATFORM.registerMobEffect("thinking", ThinkingMobEffect::new);

    /**
     * Force class init from the per-loader entry point so the static field above runs.
     */
    public static void touch() {
    }
}
