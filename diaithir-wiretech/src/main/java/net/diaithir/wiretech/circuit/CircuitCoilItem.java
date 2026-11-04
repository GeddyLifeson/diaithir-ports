package net.diaithir.wiretech.circuit;

import net.diaithir.wiretech.Machines;
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
import net.minecraft.world.phys.Vec3;

/** A red or green circuit wire. Use it on one terminal, then on another. Click the output half of a block (the side it faces) for its output, the other half for its input. Sneak-use clears the start. */
public final class CircuitCoilItem extends Item {
    public final Color color;
    public final Tier tier;

    public CircuitCoilItem(Color color, Tier tier, Properties properties) {
        super(properties);
        this.color = color;
        this.tier = tier;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof CircuitBlock cb)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        if (player != null && player.isShiftKeyDown()) {
            stack.remove(Machines.CIRCUIT_FROM.get());
            stack.remove(Machines.CIRCUIT_FROM_OUT.get());
            tell(player, "Wire start cleared.");
            return InteractionResult.SUCCESS;
        }
        boolean out = port(cb, level.getBlockState(pos).getValue(CircuitBlock.FACING), ctx.getClickLocation(), pos);
        GlobalPos from = stack.get(Machines.CIRCUIT_FROM.get());
        if (from == null || !from.dimension().equals(level.dimension())) {
            stack.set(Machines.CIRCUIT_FROM.get(), GlobalPos.of(level.dimension(), pos));
            stack.set(Machines.CIRCUIT_FROM_OUT.get(), out);
            tell(player, "Wire start set on the " + (out ? "output" : "input") + ". Use the coil on a second terminal.");
            return InteractionResult.SUCCESS;
        }
        boolean fromOut = Boolean.TRUE.equals(stack.get(Machines.CIRCUIT_FROM_OUT.get()));
        String err = CircuitNodeBE.connect((ServerLevel) level, from.pos(), fromOut, pos, out, color, tier);
        if (err != null) {
            tell(player, err);
            return InteractionResult.FAIL;
        }
        stack.remove(Machines.CIRCUIT_FROM.get());
        stack.remove(Machines.CIRCUIT_FROM_OUT.get());
        if (player == null || !player.isCreative()) stack.shrink(1);
        tell(player, color.name().toLowerCase() + " wire connected.");
        return InteractionResult.SUCCESS;
    }

    /** True for the output terminal. A block with one terminal has no choice. */
    private static boolean port(CircuitBlock cb, net.minecraft.core.Direction facing, Vec3 click, BlockPos pos) {
        if (!cb.kind.hasInput) return true;
        if (!cb.kind.hasOutput) return false;
        Vec3 rel = click.subtract(Vec3.atCenterOf(pos));
        return rel.x * facing.getStepX() + rel.z * facing.getStepZ() > 0;
    }

    private static void tell(Player p, String msg) {
        if (p != null) p.sendSystemMessage(Component.literal(msg));
    }
}
