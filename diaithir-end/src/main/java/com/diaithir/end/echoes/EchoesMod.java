package com.diaithir.end.echoes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@Mod(EchoesStructures.NS)
public final class EchoesMod {
    public static final boolean CODEX = ModList.get().isLoaded("diaithir_codex");

    public static final class EchoesEvents extends Event {
        private final Identifier id;
        private final BlockPos pos;
        private final int radius;

        public EchoesEvents(Identifier id, BlockPos pos, int radius) {
            this.id = id;
            this.pos = pos;
            this.radius = radius;
        }

        public Identifier getId() {
            return id;
        }

        public BlockPos getPos() {
            return pos;
        }

        public int getRadius() {
            return radius;
        }
    }

    public EchoesMod(IEventBus modBus, net.neoforged.fml.ModContainer container) {
        com.diaithir.end.registry.ModBlocks.register(modBus);
        com.diaithir.end.nullscape.Nullscape.init(modBus, container);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || p.tickCount % 100 != 0) return;
        ServerLevel level = (ServerLevel) p.level();
        BlockPos pos = p.blockPosition();
        var structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        for (String n : EchoesStructures.NAMES) {
            var id = Identifier.fromNamespaceAndPath(EchoesStructures.NS, n);
            ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, id);
            var holder = structures.get(key);
            if (holder.isEmpty()) continue;
            StructureStart start = level.structureManager().getStructureAt(pos, holder.get().value());
            if (start != null && start.isValid()) {
                String tag = "echoes_seen_" + n;
                if (!p.entityTags().contains(tag)) {
                    p.addTag(tag);
                    NeoForge.EVENT_BUS.post(new EchoesEvents(id, pos, 100));
                }
            }
        }
    }
}
