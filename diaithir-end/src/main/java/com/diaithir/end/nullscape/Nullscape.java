package com.diaithir.end.nullscape;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Nullscape {
 private static final Logger LOG = LoggerFactory.getLogger("diaithir_end/nullscape");

 public static void init(IEventBus modBus, ModContainer container) {
 
 container.registerConfig(ModConfig.Type.COMMON, NullscapeConfig.SPEC, "diaithir_end-nullscape.toml");
 modBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e) -> e.enqueueWork(() -> {
 if (NullscapeCompat.otherEndOverhaul()) LOG.warn("Another End/terrain overhaul detected; Nullscape biomes are injected via biome modifier only");
 NullscapeEvents.fire();
 }));
 }

 private Nullscape() {}
}
