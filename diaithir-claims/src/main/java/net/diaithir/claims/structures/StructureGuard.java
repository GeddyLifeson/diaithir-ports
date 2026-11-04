package net.diaithir.claims.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * Protects generated structures (villages, ruins, temples, bastions): inside a structure only blocks in the tag
 * diaithirclaims:structure_breakable can be broken, and nothing can be placed except torches and the tag diaithirclaims:structure_placeable.
 * Creative players and ops are exempt. Clean-room, from the description of Structural Safety (MIT, jar only, no source published).
 * Replaces the adventure-mode switching of that mod with plain event checks, so mining speed, tools and drops stay vanilla.
 */
public final class StructureGuard {
    static final TagKey<Block> BREAKABLE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("diaithirclaims", "structure_breakable"));
    static final TagKey<Block> PLACEABLE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("diaithirclaims", "structure_placeable"));

    private StructureGuard() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener((BreakBlockEvent e) -> {
            if (e.getPlayer() instanceof ServerPlayer p && inStructure(p, e.getPos()) && !exempt(p) && !e.getState().is(BREAKABLE)) {
                p.sendOverlayMessage(Component.literal("This structure is protected."));
                e.setCanceled(true);
            }
        });
        NeoForge.EVENT_BUS.addListener((BlockEvent.EntityPlaceEvent e) -> {
            if (e.getEntity() instanceof ServerPlayer p && inStructure(p, e.getPos()) && !exempt(p) && !e.getPlacedBlock().is(PLACEABLE)) {
                p.sendOverlayMessage(Component.literal("This structure is protected."));
                e.setCanceled(true);
            }
        });
    }

    private static boolean exempt(ServerPlayer p) {
        return p.isCreative() || p.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
    }

    private static boolean inStructure(ServerPlayer p, BlockPos pos) {
        ServerLevel level = p.level();
        return level.structureManager().hasAnyStructureAt(pos);
    }
}
