package net.diaithir.freight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** A shipping container as an item (cargo included). Use it on a block to set it down with its door end pointing away from you. */
public final class ContainerItem extends Item {
    public final LoadType type;

    public ContainerItem(Properties props, LoadType type) {
        super(props);
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        Direction facing = ctx.getHorizontalDirection();
        BlockPos base = ctx.getClickedPos().relative(ctx.getClickedFace());
        // the clicked cell becomes the second column, so the box stands centred on where you pointed
        BlockPos origin = base.relative(facing.getClockWise(), -1);
        ItemStack stack = ctx.getItemInHand();
        Containers.Result r = Containers.lower(level, stack, origin, facing);
        if (r == Containers.Result.OK) {
            if (ctx.getPlayer() == null || !ctx.getPlayer().isCreative()) stack.shrink(1);
        } else if (ctx.getPlayer() != null) {
            ctx.getPlayer().sendOverlayMessage(Component.literal("No room: a " + type.w() + " by " + type.l() + " block space is needed."));
        }
        return InteractionResult.SUCCESS;
    }
}
