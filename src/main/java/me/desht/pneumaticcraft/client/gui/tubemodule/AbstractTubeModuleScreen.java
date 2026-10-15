/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.TubeModuleClientRegistry;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftScreen;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractTubeModuleScreen<M extends AbstractTubeModule> extends AbstractPneumaticCraftScreen {
    protected final M module;

    AbstractTubeModuleScreen(M module) {
        super(new ItemStack(module.getItem()).getHoverName());

        this.module = module;
        this.xSize = 183;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        if (getGuiTexture() == null) {
            GuiUtils.drawScreenWithTitleArea(graphics, guiLeft, guiTop, xSize, ySize, 0xFFDDD7BA);
        }
    }

    @Override
    protected Identifier getGuiTexture() {
        return null;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Draws a 1px line one pixel at a time; replaces the old Tesselator line strip. */
    protected static void drawLine(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        for (int s = 0; s <= steps; s++) {
            int x = steps == 0 ? x0 : x0 + (x1 - x0) * s / steps;
            int y = steps == 0 ? y0 : y0 + (y1 - y0) * s / steps;
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    public static void openGuiForModule(AbstractTubeModule module) {
        Minecraft.getInstance().setScreen(TubeModuleClientRegistry.createGUI(module));
    }
}
