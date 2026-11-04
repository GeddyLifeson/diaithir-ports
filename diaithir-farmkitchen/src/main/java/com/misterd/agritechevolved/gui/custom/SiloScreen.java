package com.misterd.agritechevolved.gui.custom;

import com.misterd.agritechevolved.blockentity.custom.SiloBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SiloScreen extends AbstractContainerScreen<SiloMenu> {

    private static final Identifier GUI_TEXTURE =
            Identifier.fromNamespaceAndPath("agritechevolved", "textures/gui/auto_gui.png");

    private static final int GUI_W = 200, GUI_H = 242;

    private static final int ENERGY_BAR_X = 181;
    private static final int ENERGY_BAR_Y = 45;
    private static final int ENERGY_BAR_W = 6;
    private static final int ENERGY_BAR_H = 99;
    private static final int ENERGY_BAR_TEX_X = 200;

    public SiloScreen(SiloMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GUI_W, GUI_H);
        this.inventoryLabelY = GUI_H - 94;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE,
                this.leftPos, this.topPos, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, 256, 256);

        int energy = menu.getEnergyStored(), maxEnergy = menu.getMaxEnergyStored();
        if (maxEnergy > 0) {
            int filled = (int) (ENERGY_BAR_H * (float) energy / maxEnergy);
            if (filled > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE,
                        this.leftPos + ENERGY_BAR_X, this.topPos + ENERGY_BAR_Y + ENERGY_BAR_H - filled,
                        (float) ENERGY_BAR_TEX_X, (float) (ENERGY_BAR_H - filled),
                        ENERGY_BAR_W, filled, 256, 256);
            }
        }

        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (isOver(ENERGY_BAR_X, ENERGY_BAR_Y, ENERGY_BAR_W, ENERGY_BAR_H, mouseX, mouseY)) {
            int energy = menu.getEnergyStored(), maxEnergy = menu.getMaxEnergyStored();
            NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
            float pct = maxEnergy > 0 ? (float) energy / maxEnergy * 100.0F : 0.0F;
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("tooltip.agritechevolved.stored_energy").withStyle(ChatFormatting.YELLOW),
                    Component.literal(fmt.format(energy) + " / " + fmt.format(maxEnergy) + " RF").withStyle(ChatFormatting.GREEN),
                    Component.literal(String.format("%.1f%%", pct)).withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
            return;
        }

        if (isHovering(176, 19, 16, 16, mouseX, mouseY) && menu.slots.get(99).getItem().isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("tooltip.agritechevolved.slot.range_module")
            ), mouseX, mouseY);
            return;
        }

        super.extractTooltip(graphics, mouseX, mouseY);
    }

    private boolean isOver(int wx, int wy, int ww, int wh, int mx, int my) {
        return mx >= this.leftPos + wx && mx <= this.leftPos + wx + ww
                && my >= this.topPos + wy && my <= this.topPos + wy + wh;
    }
}