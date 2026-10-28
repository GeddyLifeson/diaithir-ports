package com.github.alexmodguy.alexscaves.server.inventory;

import com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearFurnaceBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class NuclearFurnaceResultSlot extends Slot {
    private final Player player;
    private int removeCount;

    public NuclearFurnaceResultSlot(Player player, Container furnaceContainer, int slotId, int x, int y) {
        super(furnaceContainer, slotId, x, y);
        this.player = player;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack remove(int count) {
        if (this.hasItem()) {
            this.removeCount += Math.min(count, this.getItem().getCount());
        }
        return super.remove(count);
    }

    @Override
    public void onTake(Player player, ItemStack carried) {
        this.checkTakeAchievements(carried);
        super.onTake(player, carried);
    }

    @Override
    protected void onQuickCraft(ItemStack itemStack, int i) {
        this.removeCount += i;
        this.checkTakeAchievements(itemStack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack itemStack) {
        itemStack.onCraftedBy(this.player, this.removeCount);
        if (this.player instanceof ServerPlayer serverplayer && this.container instanceof NuclearFurnaceBlockEntity nuclearFurnaceBlockEntity) {
            nuclearFurnaceBlockEntity.awardUsedRecipesAndPopExperience(serverplayer);
        }
        if (this.removeCount != 0) {
            net.neoforged.neoforge.event.EventHooks.firePlayerSmeltedEvent(this.player, itemStack, this.removeCount);
        }
        this.removeCount = 0;
    }
}
