package com.diaithir.nether.netherex;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;

public final class NetherExEvents {
    /** Global Events hook point: fired when an expanded Nether biome is first entered by a player. */
    public static class NetherBiomeEnteredEvent extends Event {
        public final Identifier biome;

        public NetherBiomeEnteredEvent(Identifier biome) {
            this.biome = biome;
        }
    }

    /** Legacy id migration table: old id path -> current path. */
    public static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("ash_stone", "ashstone"),
            Map.entry("slag_stone", "slagstone"),
            Map.entry("brimstone", "brimstone_block"),
            Map.entry("rustwood_log_block", "rustwood_log"),
            Map.entry("ember_ore_block", "ember_ore"),
            Map.entry("cinder_moss_block", "cinder_moss"),
            Map.entry("heat_resistant_glass", "heat_glass"),
            Map.entry("ember_crystal", "ember_gem"),
            Map.entry("slag_fragment", "slag_shard"),
            Map.entry("ash_cloud", "ash_wisp"),
            Map.entry("cinder_cow", "cinder_grazer"));

    private NetherExEvents() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post e) {
        if (!(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p) || p.tickCount % 200 != 0) return;
        p.level().getBiome(p.blockPosition()).unwrapKey().ifPresent(k -> {
            if (k.identifier().getNamespace().equals(NetherExModule.MOD_ID)) {
                NeoForge.EVENT_BUS.post(new NetherBiomeEnteredEvent(k.identifier()));
            }
        });
    }
}
