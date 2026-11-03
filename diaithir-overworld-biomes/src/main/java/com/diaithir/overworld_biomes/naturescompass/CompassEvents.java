package com.diaithir.overworld_biomes.naturescompass;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class CompassEvents {
    /** Global Events hook point: fired on the NeoForge bus when a biome is located. */
    public static class BiomeLocatedEvent extends Event {
        public final ServerPlayer player;
        public final Identifier biome;
        public final BlockPos pos;

        public BiomeLocatedEvent(ServerPlayer player, Identifier biome, BlockPos pos) {
            this.player = player; this.biome = biome; this.pos = pos;
        }
    }

    static void init() {
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, e -> CompassSearch.tick(e.getServer()));
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, e -> CompassSearch.clearCaches());
    }

    static void fireLocated(ServerPlayer sp, Identifier biome, BlockPos pos) {
        NeoForge.EVENT_BUS.post(new BiomeLocatedEvent(sp, biome, pos));
        if (ModList.get().isLoaded("diaithir_codex")) {
            sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable("codex.diaithir_overworld_biomes.unlocked"), true);
        }
    }

    private CompassEvents() {}
}
