package com.github.alexmodguy.alexscaves.client.gui;

import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.util.Mth;

/** Replaces the mixins on AdvancementTab: draws the cave-biome parallax background for the Alex's Caves tree. */
public class ACAdvancementTab extends AdvancementTab {
    public ACAdvancementTab(Minecraft mc, AdvancementsScreen screen, AdvancementTabType type, int index, int page, AdvancementNode node, DisplayInfo info) {
        super(mc, screen, type, index, page, node, info);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int windowLeft, int windowTop) {
        if (!centered) {
            scrollX = 117 - (maxX + minX) / 2;
            scrollY = 56 - (maxY + minY) / 2;
            centered = true;
        }
        graphics.enableScissor(windowLeft, windowTop, windowLeft + 234, windowTop + 113);
        graphics.pose().pushMatrix();
        graphics.pose().translate(windowLeft, windowTop);
        int i = Mth.floor(scrollX);
        int j = Mth.floor(scrollY);
        ACAdvancementTabs.setDimensions(234, 113);
        ACAdvancementTabs.renderTabBackground(graphics, windowLeft, windowTop, getDisplay(), scrollX, scrollY);
        root.extractConnectivity(graphics, i, j, true);
        root.extractConnectivity(graphics, i, j, false);
        root.extractRenderState(graphics, i, j);
        graphics.pose().popMatrix();
        graphics.disableScissor();
    }

    private boolean unlocked(AdvancementWidget w) {
        if (ACAdvancementTabs.Type.isTreeNodeUnlocked(w)) return true;
        var rootNode = ACAdvancementTabs.Type.typeRootNode(w.advancementNode);
        if (rootNode == null) return true;
        AdvancementWidget rw = widgets.get(rootNode.holder());
        return rw != null && rw.progress != null && rw.progress.isDone();
    }

    @Override
    public void extractTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int xo, int yo) {
        int i = Mth.floor(scrollX);
        int j = Mth.floor(scrollY);
        ACAdvancementTabs.Type hoverType = null;
        if (mouseX > 0 && mouseX < 234 && mouseY > 0 && mouseY < 113) {
            for (AdvancementWidget w : widgets.values()) {
                if (w.isMouseOver(i, j, mouseX, mouseY) && unlocked(w)) {
                    hoverType = ACAdvancementTabs.Type.forNode(w.advancementNode);
                }
            }
        }
        if (hoverType != null) { ACAdvancementTabs.setHoverType(hoverType); }
        super.extractTooltips(graphics, mouseX, mouseY, xo, yo);
    }
}
