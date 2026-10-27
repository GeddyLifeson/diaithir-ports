package rpggods.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import rpggods.RGRegistry;
import rpggods.favor.Favor;
import rpggods.favor.IFavor;

import java.util.Optional;

public class FavorContainerMenu extends AbstractContainerMenu {

    private IFavor favor;
    private Optional<Identifier> deity;

    public FavorContainerMenu(int id, final Inventory inventory) {
        this(id, inventory, Favor.EMPTY, null);
    }

    public FavorContainerMenu(final int id, final Inventory inventory, final IFavor favorIn, final Identifier deityIn) {
        super(RGRegistry.FAVOR_CONTAINER.get(), id);
        favor = favorIn;
        deity = Optional.ofNullable(deityIn);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player playerIn) {
        return true;
    }

    public IFavor getFavor() {
        return favor;
    }

    public Optional<Identifier> getDeity() {
        return deity;
    }
}
