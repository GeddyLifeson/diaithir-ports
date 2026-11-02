package com.diaithir.nether.incendium;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(IncendiumRegistries.NS)
public final class IncendiumMod {
    public IncendiumMod(IEventBus modBus, ModContainer container) {
        IncendiumRegistries.register(modBus);
        modBus.register(IncendiumEvents.class);
        NeoForge.EVENT_BUS.register(IncendiumEvents.class);
        container.registerConfig(ModConfig.Type.COMMON, IncendiumConfig.SPEC, "diaithir-nether-common.toml");
        boolean codex = ModList.get().isLoaded("diaithir_codex");
        IncendiumEvents.fireGlobalEventHook("incendium_init" + (codex ? ":codex" : ""), 3);
    }
}
