package net.diaithir.pipes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.UUID;

/** Links two pipe ends through up to {@code depth} solid blocks. First click picks a pipe, second click on a pipe of the same kind in a straight line within range links them. Clicking a linked pipe again removes the link. */
public final class ConnectorItem extends Item {
    private BlockPos pending;
    private UUID pendingOwner;

    public ConnectorItem(Properties properties) {
        super(properties);
    }

    /** Pure rule: two positions are linkable if they differ on exactly one axis by 2..maxDepth+1 blocks. */
    public static boolean linkable(BlockPos a, BlockPos b, int maxDepth) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());
        int axes = (dx > 0 ? 1 : 0) + (dy > 0 ? 1 : 0) + (dz > 0 ? 1 : 0);
        int dist = dx + dy + dz;
        return axes == 1 && dist >= 2 && dist <= maxDepth + 1;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof PipeBlock2 pb) || !(level.getBlockEntity(pos) instanceof PipeBlockEntity pipe)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        if (pipe.link() != null) {
            unlink(level, pipe);
            say(player, "Link removed");
            return InteractionResult.SUCCESS;
        }
        if (pending == null || player == null || !player.getUUID().equals(pendingOwner)) {
            pending = pos;
            pendingOwner = player == null ? null : player.getUUID();
            say(player, "First end set");
            return InteractionResult.SUCCESS;
        }
        BlockPos a = pending;
        pending = null;
        if (a.equals(pos) || !(level.getBlockEntity(a) instanceof PipeBlockEntity other) || !(level.getBlockState(a).getBlock() instanceof PipeBlock2 ob) || ob.kind() != pb.kind()) {
            say(player, "Cannot link those");
            return InteractionResult.SUCCESS;
        }
        if (!linkable(a, pos, Config.CONNECTOR_DEPTH.get())) {
            say(player, "Ends must be in a straight line, at most " + Config.CONNECTOR_DEPTH.get() + " blocks apart");
            return InteractionResult.SUCCESS;
        }
        other.setLink(pos);
        pipe.setLink(a);
        say(player, "Linked");
        return InteractionResult.SUCCESS;
    }

    private static void unlink(Level level, PipeBlockEntity pipe) {
        BlockPos other = pipe.link();
        pipe.setLink(null);
        if (other != null && level.getBlockEntity(other) instanceof PipeBlockEntity o && pipe.getBlockPos().equals(o.link())) o.setLink(null);
    }

    private static void say(Player p, String s) {
        if (p != null) p.sendOverlayMessage(Component.literal(s));
    }
}
