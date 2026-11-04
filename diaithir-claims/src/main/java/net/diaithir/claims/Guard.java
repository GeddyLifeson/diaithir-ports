package net.diaithir.claims;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/** Keeps strangers out of claimed chunks. Operators (level 2+) bypass. */
public final class Guard {
    static Optional<Claim> claimAt(LevelAccessor level, BlockPos pos) {
        if (level.isClientSide()) {
            return Optional.empty();
        }
        return level.getChunk(pos).getData(DiaithirClaims.CLAIM) instanceof Optional<Claim> c ? c : Optional.empty();
    }

    static boolean blocked(Entity who, LevelAccessor level, BlockPos pos) {
        return blocked(who, level, pos, false);
    }

    static boolean blocked(Entity who, LevelAccessor level, BlockPos pos, boolean use) {
        Optional<Claim> c = claimAt(level, pos);
        if (c.isEmpty()) {
            return false;
        }
        if (who instanceof ServerPlayer p) {
            if (use && c.get().has(Claim.PUBLIC_USE)) {
                return false;
            }
            if (c.get().allows(p.getUUID()) || p.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
                return false;
            }
            p.sendSystemMessage(Component.literal("This chunk is claimed by " + c.get().ownerName() + "."), true);
        }
        return true;
    }

    @SubscribeEvent
    public void onBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent e) {
        if (blocked(e.getPlayer(), e.getLevel(), e.getPos())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (e.getEntity() != null && blocked(e.getEntity(), e.getLevel(), e.getPos())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onUse(PlayerInteractEvent.RightClickBlock e) {
        Player p = e.getEntity();
        if (!p.level().isClientSide() && blocked(p, p.level(), e.getPos(), true)) {
            e.setCanceled(true);
        }
    }

    /** Drops every claimed position from an explosion's block list. */
    static void strip(LevelAccessor level, java.util.List<BlockPos> blocks) {
        blocks.removeIf(pos -> claimAt(level, pos).filter(c -> !c.has(Claim.ALLOW_EXPLOSIONS)).isPresent());
    }

    @SubscribeEvent
    public void onBoom(ExplosionEvent.Detonate e) {
        strip(e.getLevel(), e.getAffectedBlocks());
    }
}
