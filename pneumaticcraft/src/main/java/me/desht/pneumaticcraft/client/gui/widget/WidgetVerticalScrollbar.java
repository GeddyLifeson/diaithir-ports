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

package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.lib.Textures;
import me.desht.pneumaticcraft.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;
import org.joml.Matrix4f;


public class WidgetVerticalScrollbar extends AbstractWidget {
    public float currentScroll;
    private int states;
    private boolean listening;
    private boolean dragging;

    public WidgetVerticalScrollbar(int x, int y, int height) {
        super(x, y, 14, height, Component.empty());
    }

    public WidgetVerticalScrollbar setStates(int states) {
        this.states = states;
        return this;
    }

    public WidgetVerticalScrollbar setCurrentState(int state) {
        Validate.isTrue(state >= 0 && state <= states, "State " + state + " out of range! Valid range [1 - " + states + "] inclusive");
        currentScroll = (float) state / states;
        return this;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dirX, double dirY) {
        if (active && listening) {
            double wheel = Mth.clamp(-dirY, -1, 1);
            currentScroll = Mth.clamp(currentScroll + (float) wheel / states,0f, 1);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(net.minecraft.client.input.MouseButtonEvent _ev, boolean _dbl) {
        double x = _ev.x(), y = _ev.y(); int button = _ev.button();
        currentScroll = (float) (y - 7 - this.getY()) / (height - 17);
        currentScroll = Mth.clamp(currentScroll, 0, 1);
        dragging = true;
    }

    @Override
    public void onRelease(net.minecraft.client.input.MouseButtonEvent _ev) {
        double x = _ev.x(), y = _ev.y();
        dragging = false;
    }

    @Override
    protected void onDrag(net.minecraft.client.input.MouseButtonEvent _ev, double dx, double dy) {
        double x = _ev.x(), y = _ev.y();
        dragging = true;
        currentScroll = (float) (y - 7 - this.getY()) / (height - 17);
        currentScroll = Mth.clamp(currentScroll, 0, 1);
    }

    public WidgetVerticalScrollbar setListening(boolean listening) {
        this.listening = listening;
        return this;
    }

    public int getState() {
        float scroll = currentScroll;
        scroll += 0.5F / states;
        return Mth.clamp((int) (scroll * states), 0, states);
    }

    @Override
    public void extractWidgetRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics_raw, int mouseX, int mouseY, float partialTick) {
        GuiGraphics graphics = GuiGraphics.of(graphics_raw);
        if (visible) {
            int x = getX(), y = getY();
            graphics.blit(Textures.WIDGET_VERTICAL_SCROLLBAR, x, y, 12, 0, width, 1, 26, 15);
            for (int i = 0; i < height - 2; i++) {
                graphics.blit(Textures.WIDGET_VERTICAL_SCROLLBAR, x, y + 1 + i, 12, 1, width, 1, 26, 15);
            }
            graphics.blit(Textures.WIDGET_VERTICAL_SCROLLBAR, x, y + height - 1, 12, 14, width, 1, 26, 15);

            if (!active) graphics.setColor(0.6F, 0.6F, 0.6F, 1);
            graphics.blit(Textures.WIDGET_VERTICAL_SCROLLBAR, x + 1, y + 1 + (int) ((height - 17) * currentScroll), 0, 0, 12, 15, 26, 15);
            graphics.setColor(1, 1, 1, 1);
        }
    }

    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}
