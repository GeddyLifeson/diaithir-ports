package net.diaithir.pipes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Right click a pipe face to cycle its mode. Sneak plus click on the face cycles redstone mode; sneak plus click on the opposite face cycles distribution; sneak on the up face opens the filter menu. */
public final class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof PipeBlock2) || !(level.getBlockEntity(pos) instanceof PipeBlockEntity pipe)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        Direction face = ctx.getClickedFace();
        if (player != null && player.isShiftKeyDown()) {
            if (face == Direction.UP && player instanceof ServerPlayer sp) {
                sp.openMenu(new SimpleMenuProvider((id, inv, p) -> new FilterMenu(id, inv, pipe), Component.literal("Pipe filter")));
                return InteractionResult.SUCCESS;
            }
            if (face == Direction.DOWN) {
                pipe.cycleDistribution();
                say(player, "Distribution: " + pipe.distribution());
            } else {
                pipe.cycleRedstone();
                say(player, "Redstone: " + pipe.redstone());
            }
            return InteractionResult.SUCCESS;
        }
        FaceMode next = pipe.mode(face).next();
        pipe.setMode(face, next);
        say(player, face.getName() + ": " + next);
        return InteractionResult.SUCCESS;
    }

    private static void say(Player p, String s) {
        if (p != null) p.sendOverlayMessage(Component.literal(s));
    }
}
