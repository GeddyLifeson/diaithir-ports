package com.misterd.agritechevolved.gui.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CapacitorScreen extends AbstractContainerScreen<CapacitorMenu> {

    private static final Identifier GUI_TEXTURE =
            Identifier.fromNamespaceAndPath("agritechevolved", "textures/gui/capacitor_gui.png");

    private static final int GUI_W = 176, GUI_H = 154;

    private static final int BAR_X = 8;
    private static final int BAR_Y = 19;
    private static final int BAR_W = 160;
    private static final int BAR_H = 34;

    public CapacitorScreen(CapacitorMenu menu, Inventory playerInventory, Component title) {
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
            int fillWidth = (int) (BAR_W * (double) energy / maxEnergy);
            if (fillWidth > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE,
                        this.leftPos + BAR_X, this.topPos + BAR_Y,
                        0.0F, 154.0F,
                        fillWidth, BAR_H, 256, 256);
            }
        }

        Component energyText = Component.literal(NumberFormat.getNumberInstance(Locale.US).format(energy) + " RF");
        int barCenterX = this.leftPos + BAR_X + BAR_W / 2;
        int barCenterY = this.topPos  + BAR_Y + BAR_H / 2 - this.minecraft.font.lineHeight;
        graphics.text(this.minecraft.font, energyText,
                barCenterX - this.minecraft.font.width(energyText) / 2, barCenterY, 0xFFFFFFFF, true);

        if (maxEnergy > 0) {
            Component pctText = Component.literal(String.format("%.1f%%", (double) energy * 100.0 / maxEnergy));
            graphics.text(this.minecraft.font, pctText,
                    barCenterX - this.minecraft.font.width(pctText) / 2, barCenterY + 10, 0xFFCCCCCC, true);
        }

        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (isOver(BAR_X, BAR_Y, BAR_W, BAR_H, mouseX, mouseY)) {
            int energy = menu.getEnergyStored(), maxEnergy = menu.getMaxEnergyStored();
            int transferRate = menu.getTransferRate();
            String tierName = menu.getTierName();
            NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
            double pct = maxEnergy > 0 ? (double) energy * 100.0 / maxEnergy : 0.0;
            graphics.setComponentTooltipForNextFrame(this.minecraft.font, List.of(
                    Component.translatable("tooltip.agritechevolved.capacitor.title", tierName).withStyle(ChatFormatting.GOLD),
                    Component.translatable("tooltip.agritechevolved.capacitor.energy_storage").withStyle(ChatFormatting.YELLOW),
                    Component.translatable("tooltip.agritechevolved.capacitor.energy",
                            fmt.format(energy), fmt.format(maxEnergy)).withStyle(ChatFormatting.WHITE),
                    Component.translatable("tooltip.agritechevolved.capacitor.energy_percentage",
                            String.format("%.1f", pct)).withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.agritechevolved.capacitor.transfer_rate",
                            fmt.format(transferRate)).withStyle(ChatFormatting.AQUA)
            ), mouseX, mouseY);
            return;
        }

        if (isOver(BAR_X, 6, BAR_W, 8, mouseX, mouseY)) {
            String tierName = menu.getTierName();
            int transferRate = menu.getTransferRate();
            graphics.setComponentTooltipForNextFrame(this.minecraft.font, List.of(
                    Component.translatable("tooltip.agritechevolved.capacitor.title", tierName).withStyle(ChatFormatting.GOLD),
                    Component.translatable("tooltip.agritechevolved.capacitor.description").withStyle(ChatFormatting.YELLOW),
                    Component.translatable("tooltip.agritechevolved.capacitor.max_transfer",
                            NumberFormat.getNumberInstance(Locale.US).format(transferRate)).withStyle(ChatFormatting.AQUA)
            ), mouseX, mouseY);
            return;
        }

        if (isOver(BAR_X, 50, BAR_W, 15, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(this.minecraft.font, List.of(
                    Component.translatable("tooltip.agritechevolved.capacitor.information").withStyle(ChatFormatting.GOLD),
                    Component.translatable("tooltip.agritechevolved.capacitor.info_stores").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.agritechevolved.capacitor.info_balances").withStyle(ChatFormatting.GRAY),
                    Component.translatable("tooltip.agritechevolved.capacitor.info_automatic").withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
            return;
        }

        super.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return super.mouseClicked(event, doubleClick);
    }

    private boolean isOver(int wx, int wy, int ww, int wh, int mx, int my) {
        return mx >= this.leftPos + wx && mx <= this.leftPos + wx + ww
                && my >= this.topPos + wy && my <= this.topPos + wy + wh;
    }
}