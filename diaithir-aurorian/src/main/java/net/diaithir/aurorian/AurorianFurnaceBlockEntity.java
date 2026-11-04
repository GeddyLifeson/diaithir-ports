package net.diaithir.aurorian;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;

public class AurorianFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    /** Upstream defaults: up to 10 chimneys, fuel x2.5 at the maximum. */
    private static final int MAX_CHIMNEYS = 10;
    private static final float MAX_MULTIPLIER = 2.5F;

    public AurorianFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirAurorian.FURNACE_BE.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.theaurorian.aurorian_furnace");
    }

    @Override
    protected int getBurnDuration(FuelValues fuelValues, ItemStack fuel) {
        int base = super.getBurnDuration(fuelValues, fuel);
        if (level == null) {
            return base;
        }
        int n = 0;
        BlockPos p = worldPosition.above();
        while (n < MAX_CHIMNEYS && level.getBlockState(p).is(DiaithirAurorian.CHIMNEY.get())) {
            n++;
            p = p.above();
        }
        return (int) (base * Math.max(1.0F, (float) n / MAX_CHIMNEYS * MAX_MULTIPLIER));
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new FurnaceMenu(id, inventory, this, this.dataAccess);
    }
}
