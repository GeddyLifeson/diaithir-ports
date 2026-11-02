package com.diaithir.overworld_biomes.yungscavebiomes;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

public final class CaveBiomesModule {
    private CaveBiomesModule() {}

    public static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, CaveBiomesConfig.SPEC, "diaithir_overworld_biomes-common.toml");
        CaveBiomesEvents.register(modBus);
        CaveBiomeContent.register(modBus);
    }

}
