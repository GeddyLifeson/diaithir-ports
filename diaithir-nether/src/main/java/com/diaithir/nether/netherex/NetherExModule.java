package com.diaithir.nether.netherex;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

public final class NetherExModule {
    public static final String MOD_ID = "diaithir_nether";
    public static final boolean TELLUS = ModList.get().isLoaded("tellus");
    public static final boolean TERRALITH = ModList.get().isLoaded("terralith");
    public static final boolean CODEX = ModList.get().isLoaded("diaithir_codex");

    private NetherExModule() {}

    public static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, NetherExConfig.SPEC, "diaithir_nether-netherex.toml");
        NetherExRegistries.register(modBus);
        modBus.addListener(NetherExRegistries::onAttributes);
        modBus.addListener(NetherExRegistries::onSpawnPlacements);
        NeoForge.EVENT_BUS.register(NetherExEvents.class);
    }
}
