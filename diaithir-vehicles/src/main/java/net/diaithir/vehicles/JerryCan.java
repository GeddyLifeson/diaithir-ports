package net.diaithir.vehicles;

import net.diaithir.vehicles.fuel.Fuel;
import net.diaithir.vehicles.fuel.FuelCaps;
import net.diaithir.vehicles.fuel.FuelStore;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** A 20 L fuel can. Use on a tank or pump to pour in; sneak-use to draw out. Right-click a vehicle to refuel it. */
public class JerryCan extends Item {
    public static final long CAPACITY_L = 20;

    public JerryCan(Properties p) {
        super(p);
    }

    /** The can as a fuel store: contents live in the {@code diaithirvehicles:fuel} data component. */
    public static final class CanStore implements FuelStore {
        private final ItemStack stack;

        public CanStore(ItemStack stack) {
            this.stack = stack;
        }

        private CanContent c() {
            return stack.getOrDefault(DiaithirVehicles.CAN_CONTENT.get(), CanContent.EMPTY);
        }

        @Override
        public long capacityL() {
            return CAPACITY_L;
        }

        @Override
        public long amountL() {
            return c().amount();
        }

        @Override
        public String fuelId() {
            return c().amount() > 0 ? c().fuel() : null;
        }

        @Override
        public long fill(String fuelId, long l, boolean simulate) {
            if (fuelId == null || l <= 0 || Fuel.get(fuelId) == null) return 0;
            CanContent cur = c();
            if (cur.amount() > 0 && !cur.fuel().equals(fuelId)) return 0;
            long put = Math.min(l, CAPACITY_L - cur.amount());
            if (put > 0 && !simulate) stack.set(DiaithirVehicles.CAN_CONTENT.get(), new CanContent(fuelId, cur.amount() + put));
            return Math.max(put, 0);
        }

        @Override
        public long drain(long l, boolean simulate) {
            CanContent cur = c();
            long took = Math.min(Math.max(l, 0), cur.amount());
            if (took > 0 && !simulate) {
                if (cur.amount() - took == 0) stack.remove(DiaithirVehicles.CAN_CONTENT.get());
                else stack.set(DiaithirVehicles.CAN_CONTENT.get(), new CanContent(cur.fuel(), cur.amount() - took));
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
        long moved = draw ? target.transferTo(can, CAPACITY_L) : can.transferTo(target, CAPACITY_L);
        if (ctx.getPlayer() != null)
            ctx.getPlayer().sendOverlayMessage(Component.literal(moved > 0 ? (draw ? "Drew " : "Poured ") + moved + " L" : draw ? "Nothing to draw." : "Nothing poured."));
        return moved > 0 ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
