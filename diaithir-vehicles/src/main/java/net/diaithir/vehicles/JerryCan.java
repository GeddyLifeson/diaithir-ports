package net.diaithir.vehicles;

import net.diaithir.units.fuel.Fuel;
import net.diaithir.units.fuel.FuelStore;
import net.diaithir.units.fuel.Fuels;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** A 20 L fuel can. Use on a tank or pump to pour in; sneak-use to draw out. Right-click a vehicle to refuel it. */
public class JerryCan extends Item {
    public static final double CAPACITY_L = 20;

    public JerryCan(Properties p) {
        super(p);
    }

    /** The can as a {@link FuelStore}: contents live in the {@code diaithirvehicles:fuel} data component. Liquid fuels only. */
    public static final class CanStore implements FuelStore {
        private final ItemStack stack;

        public CanStore(ItemStack stack) {
            this.stack = stack;
        }

        private CanContent c() {
            return stack.getOrDefault(DiaithirVehicles.CAN_CONTENT.get(), CanContent.EMPTY);
        }

        @Override
        public Fuel fuel() {
            return c().litres() > 0 ? Fuels.get(c().fuel()) : null;
        }

        @Override
        public double quantity() {
            return c().litres();
        }

        @Override
        public double capacity() {
            return CAPACITY_L;
        }

        @Override
        public double insert(Fuel f, double amount) {
            CanContent cur = c();
            if (f == null || f.kind() != Fuel.Kind.LIQUID || amount <= 0 || (cur.litres() > 0 && !cur.fuel().equals(f.id()))) return 0;
            double put = Math.min(amount, CAPACITY_L - cur.litres());
            if (put > 0) stack.set(DiaithirVehicles.CAN_CONTENT.get(), new CanContent(f.id(), cur.litres() + put));
            return Math.max(put, 0);
        }

        @Override
        public double extract(double amount) {
            CanContent cur = c();
            double took = Math.max(0, Math.min(amount, cur.litres()));
            if (took > 0) {
                if (cur.litres() - took <= 1e-9) stack.remove(DiaithirVehicles.CAN_CONTENT.get());
                else stack.set(DiaithirVehicles.CAN_CONTENT.get(), new CanContent(cur.fuel(), cur.litres() - took));
            }
            return took;
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        FuelStore target = FuelLookup.block(ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace());
        if (target == null) return InteractionResult.PASS;
        FuelStore can = new CanStore(ctx.getItemInHand());
        boolean draw = ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown();
        double moved = draw ? FuelStore.transfer(target, can, CAPACITY_L) : FuelStore.transfer(can, target, CAPACITY_L);
        if (ctx.getPlayer() != null)
            ctx.getPlayer().sendOverlayMessage(Component.literal(moved > 0 ? (draw ? "Drew " : "Poured ") + Math.round(moved * 10) / 10.0 + " L" : draw ? "Nothing to draw." : "Nothing poured."));
        return moved > 0 ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
