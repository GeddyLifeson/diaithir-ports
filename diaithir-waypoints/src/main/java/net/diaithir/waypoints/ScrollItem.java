package net.diaithir.waypoints;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Used up to walk back to the waypoint you lit most recently, from anywhere. */
public final class ScrollItem extends Item {
    public ScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer sp)) {
            return InteractionResult.SUCCESS;
        }
        List<Known> known = sp.getData(DiaithirWaypoints.KNOWN);
        if (known.isEmpty()) {
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("You have not lit a waypoint yet."));
            return InteractionResult.FAIL;
        }
        if (Travel.go(sp, known.size() - 1, true)) {
            ItemStack stack = player.getItemInHand(hand);
            stack.shrink(1);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }
}
