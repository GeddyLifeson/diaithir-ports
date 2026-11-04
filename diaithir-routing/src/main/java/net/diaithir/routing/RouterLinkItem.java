package net.diaithir.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Binds two routers: use on the first, then on the second. Both must share a dimension. Sneak-use on a router clears its links. */
public final class RouterLinkItem extends Item {
    public RouterLinkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (level.isClientSide()) return level.getBlockState(pos).getBlock() instanceof RouterBlock ? InteractionResult.SUCCESS : InteractionResult.PASS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RouterBlockEntity router)) return InteractionResult.PASS;
        ItemStack stack = ctx.getItemInHand();
        var player = ctx.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            router.clearLinks();
            player.sendOverlayMessage(Component.literal("Router links cleared"));
            return InteractionResult.SUCCESS;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("dim")) {
            tag.putString("dim", level.dimension().identifier().toString());
            tag.putLong("pos", pos.asLong());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            if (player != null) player.sendOverlayMessage(Component.literal("First router stored"));
            return InteractionResult.SUCCESS;
        }
        String dim = tag.getStringOr("dim", "");
        BlockPos first = BlockPos.of(tag.getLongOr("pos", 0L));
        stack.remove(DataComponents.CUSTOM_DATA);
        if (!dim.equals(level.dimension().identifier().toString())) {
            if (player != null) player.sendOverlayMessage(Component.literal("Routers must share a dimension"));
            return InteractionResult.FAIL;
        }
        if (first.equals(pos)) return InteractionResult.FAIL;
        boolean ok = router.link(GlobalPos.of(level.dimension(), first)) && level.getBlockEntity(first) instanceof RouterBlockEntity other
                && other.link(GlobalPos.of(level.dimension(), pos));
        if (player != null) player.sendOverlayMessage(Component.literal(ok ? "Routers linked" : "Link failed: out of range or no free slot"));
        return ok ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
