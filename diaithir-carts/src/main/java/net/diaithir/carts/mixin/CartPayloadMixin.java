package net.diaithir.carts.mixin;

import net.diaithir.carts.CartLogic;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

/** Narrow-gauge freight carts take cargo only in their first slots (Gauge.payload): hoppers, loaders and players are refused the rest. */
@Mixin(AbstractMinecartContainer.class)
abstract class CartPayloadMixin {
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < CartLogic.slotLimit((AbstractMinecart) (Object) this);
    }
}
