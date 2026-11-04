package net.diaithir.freight;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Rigging for lifting a container by hand where no crane stands: use it on any wall or door of a deployed container and the whole box, cargo and all, becomes
 * one item. It bears {@link #LIFT_KG}. Cranes and forklifts call {@link Containers#lift} with their own rating instead.
 */
public final class CargoHook extends Item {
    public static final double LIFT_KG = 35_000;

    public CargoHook(Properties props) { super(props); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        if (!Containers.isShell(level.getBlockState(ctx.getClickedPos()).getBlock())) return InteractionResult.PASS;
        Containers.Lift lift = Containers.lift(level, ctx.getClickedPos(), LIFT_KG, false);
        if (lift.result() == Containers.Result.OK) {
            if (ctx.getPlayer() != null && !ctx.getPlayer().getInventory().add(lift.stack())) ctx.getPlayer().drop(lift.stack(), false);
        } else if (ctx.getPlayer() != null) {
            ctx.getPlayer().sendOverlayMessage(Component.literal(switch (lift.result()) {
                case TOO_HEAVY -> "Too heavy for the hook: " + (int) LIFT_KG + " kg at most.";
                case NOT_EMPTY -> "Pump the tank empty first.";
                default -> "Not a container.";
            }));
        }
        return InteractionResult.SUCCESS;
    }
}
