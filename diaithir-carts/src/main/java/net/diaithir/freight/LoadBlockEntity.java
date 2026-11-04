package net.diaithir.freight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** The items of a pallet, barrel, chest or crate. Opens a plain chest screen when the load stands loose; inside a container it is reached only through a dock. */
public final class LoadBlockEntity extends BaseContainerBlockEntity {
    public final LoadType type;
    private NonNullList<ItemStack> items;

    public LoadBlockEntity(BlockPos pos, BlockState state) {
        super(Freight.LOAD_BE.get(), pos, state);
        this.type = ((LoadBlock) state.getBlock()).type;
        this.items = NonNullList.withSize(type.itemSlots(), ItemStack.EMPTY);
    }

    public double payloadKg() { return Loads.kg(items); }

    @Override public int getContainerSize() { return items.size(); }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override protected Component getDefaultName() { return getBlockState().getBlock().getName(); }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        int rows = items.size() / 9;
        MenuType<?> menu = rows <= 2 ? MenuType.GENERIC_9x2 : rows <= 3 ? MenuType.GENERIC_9x3 : MenuType.GENERIC_9x6;
        return new ChestMenu(menu, id, inventory, this, rows);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, items);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        items = NonNullList.withSize(type.itemSlots(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, items);
    }
}
