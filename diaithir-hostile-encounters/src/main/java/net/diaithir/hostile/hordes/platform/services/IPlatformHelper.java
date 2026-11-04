package net.diaithir.hostile.hordes.platform.services;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

import java.util.function.Supplier;

public interface IPlatformHelper {
    String getPlatformName();

    boolean isModLoaded(String modId);

    /**
     * Register a MobEffect under id "{horde_hoard}:effectId" and return its Holder.
     * Both Fabric (vanilla Registry.register) and NeoForge (DeferredRegister) have
     * to be able to honor this — the Holder<MobEffect> form is what 26.1.2's
     * MobEffectInstance / addEffect / hasEffect APIs all consume.
     */
    Holder<MobEffect> registerMobEffect(String effectId, Supplier<MobEffect> effect);

    /**
     * Register a boolean game rule. In 26.1.2, BuiltInRegistries.GAME_RULE freezes
     * before mod constructor runs, so registration is loader-specific:
     *   - Fabric uses GameRuleBuilder.buildAndRegister at ModInitializer time.
     *   - NeoForge uses the AT'd GameRules.registerBoolean, called from a RegisterEvent
     *     listener for the GAME_RULE registry.
     * The id must be a valid Identifier path ([a-z0-9/._-] only) — uppercase letters
     * are rejected by Identifier.assertValidPath in 26.1.2 (the cause of the 1.20.1
     * source's hordeStacking/hordeMultiplying/ironGolemRegenPower having to become
     * snake_case in this port).
     */
    GameRule<Boolean> registerBooleanGameRule(String name, GameRuleCategory category, boolean defaultValue);

    /**
     * Register an integer game rule with a minimum bound. Same loader-specific timing
     * notes as {@link #registerBooleanGameRule}.
     */
    GameRule<Integer> registerIntegerGameRule(String name, GameRuleCategory category, int defaultValue, int min);
}
