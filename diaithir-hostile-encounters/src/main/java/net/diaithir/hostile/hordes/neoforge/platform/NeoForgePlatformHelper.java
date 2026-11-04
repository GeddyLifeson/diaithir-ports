package net.diaithir.hostile.hordes.neoforge.platform;

import net.diaithir.hostile.hordes.HordeHoardConstants;
import net.diaithir.hostile.hordes.platform.services.IPlatformHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgePlatformHelper implements IPlatformHelper {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, HordeHoardConstants.MOD_ID);

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Holder<MobEffect> registerMobEffect(String effectId, Supplier<MobEffect> effect) {
        DeferredHolder<MobEffect, MobEffect> holder = MOB_EFFECTS.register(effectId, effect);
        return holder;
    }

    /**
     * Calls the AT'd GameRules.registerBoolean. Only safe to invoke from a RegisterEvent
     * listener for the GAME_RULE registry — outside that phase, BuiltInRegistries.GAME_RULE
     * is frozen and the underlying Registry.register throws IllegalStateException.
     */
    @Override
    public GameRule<Boolean> registerBooleanGameRule(String name, GameRuleCategory category, boolean defaultValue) {
        return GameRules.registerBoolean(name, category, defaultValue);
    }

    @Override
    public GameRule<Integer> registerIntegerGameRule(String name, GameRuleCategory category, int defaultValue, int min) {
        return GameRules.registerInteger(name, category, defaultValue, min);
    }

    /**
     * Helper for the unused Identifier import — kept here so the file's imports stay
     * consistent with FabricPlatformHelper's structure.
     */
    @SuppressWarnings("unused")
    private static Identifier modId(String path) {
        return Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, path);
    }
}
