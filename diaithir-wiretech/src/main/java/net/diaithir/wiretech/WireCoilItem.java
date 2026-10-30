package net.diaithir.wiretech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** A coil of wire. Use it on a connector to start a wire, then on a second connector to finish it. Sneak-use clears the start. */
public final class WireCoilItem extends Item {
    public final WireType wire;

    public WireCoilItem(WireType wire, Properties properties) {
        super(properties);
        this.wire = wire;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof ConnectorBlock cb)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        if (cb.wire != wire) {
            tell(player, "A " + wire.id + " wire only fits " + wire.id + " connectors.");
            return InteractionResult.FAIL;
        }
        if (player != null && player.isShiftKeyDown()) {
            stack.remove(Machines.WIRE_FROM.get());
            tell(player, "Wire start cleared.");
            return InteractionResult.SUCCESS;
        }
        GlobalPos from = stack.get(Machines.WIRE_FROM.get());
        if (from == null || !from.dimension().equals(level.dimension())) {
            stack.set(Machines.WIRE_FROM.get(), GlobalPos.of(level.dimension(), pos));
            tell(player, "Wire start set. Use the coil on a second connector.");
            return InteractionResult.SUCCESS;
        }
        String err = ConnectorBlockEntity.connect((ServerLevel) level, from.pos(), pos, wire);
        if (err != null) {
            tell(player, err);
            return InteractionResult.FAIL;
        }
        stack.remove(Machines.WIRE_FROM.get());
        if (player == null || !player.isCreative()) stack.shrink(1);
        tell(player, "Wire connected.");
        return InteractionResult.SUCCESS;
    }

    private static void tell(Player p, String msg) {
        if (p != null) p.sendSystemMessage(Component.literal(msg));
    }
}
