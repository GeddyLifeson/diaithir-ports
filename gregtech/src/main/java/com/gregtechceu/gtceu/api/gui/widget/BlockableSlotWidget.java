package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.world.Container;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/** Basically just your normal SlotWidget, but can render the slot as "grayed-out" with a Supplier value. */
public class BlockableSlotWidget extends SlotWidget {

    private static final int OVERLAY_COLOR = 0x80404040;

    private BooleanSupplier isBlocked = () -> false;

    public BlockableSlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition, boolean canTakeItems,
                               boolean canPutItems) {
        super(inventory, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public BlockableSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition,
                               boolean canTakeItems, boolean canPutItems) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public BlockableSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition);
    }

    public BlockableSlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition) {
        super(inventory, slotIndex, xPosition, yPosition);
    }

    public BlockableSlotWidget setIsBlocked(BooleanSupplier isBlocked) {
        this.isBlocked = isBlocked;
        return this;
    }

    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    @Override
    public boolean isMouseOverElement(double mouseX, double mouseY) {
        // prevent slot removal and hover highlighting when slot is blocked
        return super.isMouseOverElement(mouseX, mouseY) && !isBlocked.getAsBoolean();
    }
}
