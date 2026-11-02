package com.diaithir.overworld_biomes.blooming;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(BloomingBiosphere.MOD_ID)
public final class BloomingBiosphere {
    public static final String MOD_ID = "diaithir_overworld_biomes";

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MOD_ID);

    static {
        CONDITIONS.register("blooming_toggle", () -> BloomingToggleCondition.CODEC);
    }

    public BloomingBiosphere(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, BloomingConfig.SPEC);
        CONDITIONS.register(modBus);
        modBus.addListener(BloomingMigration::registerMigration);
        BloomingEvents.fireWorldgenHook(NeoForge.EVENT_BUS);
    }
}
