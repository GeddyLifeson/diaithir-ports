// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: config access via ClientConfig statics.
package net.diaithir.hudoverlays.pickupnotifier.client.gui;

import java.util.List;
import java.util.Objects;
import net.diaithir.hudoverlays.pickupnotifier.client.config.ClientConfig;
import net.diaithir.hudoverlays.pickupnotifier.client.util.DisplayEntryRenderHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class ItemDisplayEntry extends DisplayEntry<ItemStack> {
    private static final int ITEM_STACK_POP_TIME = 5;

    public ItemDisplayEntry(ItemStack itemStack, int displayAmount) {
        super(itemStack.copy(), displayAmount, itemStack.getRarity());
        this.item.setPopTime(ITEM_STACK_POP_TIME);
    }

    @Override
    protected Component getEntryName(ItemStack itemStack) {
        if (ClientConfig.COMBINE_ENTRIES.get() == ClientConfig.CombineEntries.ALWAYS) {
            return itemStack.getItem().getName(itemStack);
        }
        return itemStack.getHoverName();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.item.getPopTime() > 0) {
            this.item.setPopTime(this.item.getPopTime() - 1);
        }
    }

    @Override
    public DisplayEntry<?> mergeWith(DisplayEntry<?> otherDisplayEntry) {
        return new ItemDisplayEntry(this.item, this.getDisplayAmount() + otherDisplayEntry.getDisplayAmount());
    }

    @Override
    protected void appendTextComponents(List<Component> components, boolean reverse) {
        if (ClientConfig.INVENTORY_COUNT.get()) {
            Component count = Component.literal(String.valueOf(this.getInventoryCount()));
            components.add(Component.literal("(").append(count).append(")"));
        }
        super.appendTextComponents(components, reverse);
    }

    private int getInventoryCount() {
        if (Minecraft.getInstance().player == null) {
            return 0;
        }
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        return ContainerHelper.clearOrCountMatchingItems(inventory,
                (ItemStack other) -> ItemStack.isSameItem(this.item, other), 0, true);
    }

    @Override
    protected void renderSprite(GuiGraphicsExtractor guiGraphics, Font font, int posX, int posY, float alpha) {
        guiGraphics.item(this.item, posX, posY);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        ItemDisplayEntry other = (ItemDisplayEntry) obj;
        return ItemStack.isSameItemSameComponents(this.item, other.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.item.getItem());
    }
}
