package com.diaithir.overworldbiomes.biomesoplenty;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

@Mod(BiomesOPlentyModule.MOD_ID)
public final class BiomesOPlentyModule {
    public static final String MOD_ID = "diaithir_overworld_biomes";
    public static final boolean TELLUS = ModList.get() != null && ModList.get().isLoaded("tellus");
    public static final boolean TERRALITH = ModList.get() != null && ModList.get().isLoaded("terralith");
    public static final boolean CODEX = ModList.get() != null && ModList.get().isLoaded("codex");

    public BiomesOPlentyModule(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, BiomeConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(GlobalEventsHook::onBiomesLoaded);
        GlobalEventsHook.fire("module_loaded", MOD_ID);
    }
}
