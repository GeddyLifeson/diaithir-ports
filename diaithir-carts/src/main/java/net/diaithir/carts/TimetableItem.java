package net.diaithir.carts;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** The timetable. Use it on station rails in order to write the stops, sneak and use in the air to wipe it. Use it on a cart (see DiaithirCarts) to give the cart the schedule. */
public class TimetableItem extends Item {
    public TimetableItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        BlockState st = ctx.getLevel().getBlockState(pos);
        Player pl = ctx.getPlayer();
        if (!(st.getBlock() instanceof StationRail)) return InteractionResult.PASS;
        if (!ctx.getLevel().isClientSide() && pl != null) {
            ItemStack stack = ctx.getItemInHand();
            Schedule s = stack.getOrDefault(Schedule.ITEM.get(), new Schedule(List.of(), 0));
            Schedule n = s.add(pos);
            stack.set(Schedule.ITEM.get(), n);
            pl.sendOverlayMessage(Component.literal(n == s ? "Stop not added" : "Stop " + n.stops().size() + " written"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(net.minecraft.world.level.Level level, Player pl, net.minecraft.world.InteractionHand hand) {
        if (!pl.isShiftKeyDown()) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            pl.getItemInHand(hand).remove(Schedule.ITEM.get());
            pl.sendOverlayMessage(Component.literal("Timetable wiped"));
        }
        return InteractionResult.SUCCESS;
    }
}
