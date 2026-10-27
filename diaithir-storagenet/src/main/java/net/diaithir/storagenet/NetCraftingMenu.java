package net.diaithir.storagenet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

/**
 * A crafting table that crafts from the whole network: after each click, any grid slot that was in use and has run empty is refilled
 * with one of the same item from the network. Hold a stack, craft and shift-craft as usual; the grid keeps feeding itself.
 */
public final class NetCraftingMenu extends CraftingMenu {
    private final Level level;
    private final BlockPos pos;

    public NetCraftingMenu(int id, Inventory inv, ContainerLevelAccess access, Level level, BlockPos pos) {
        super(id, inv, access);
        this.level = level;
        this.pos = pos;
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput input, Player player) {
        List<Slot> grid = getInputGridSlots();
        ItemResource[] before = new ItemResource[grid.size()];
        for (int i = 0; i < before.length; i++) {
            ItemStack st = grid.get(i).getItem();
            before[i] = st.isEmpty() ? null : ItemResource.of(st);
        }
        super.clicked(slotIndex, button, input, player);
        if (level.isClientSide()) return;
        Network net = Networks.find(level, pos);
        if (net == null) return;
        boolean changed = false;
        for (int i = 0; i < before.length; i++) {
            if (before[i] == null || !grid.get(i).getItem().isEmpty()) continue;
            ItemStack got = net.extractStack(before[i], 1);
            if (!got.isEmpty()) {
                grid.get(i).setByPlayer(got);
                changed = true;
            }
        }
        if (changed) broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return level.getBlockState(pos).getBlock() instanceof TerminalBlock
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= Math.pow(Config.MAX_TERMINAL_DISTANCE.get(), 2);
    }
}
