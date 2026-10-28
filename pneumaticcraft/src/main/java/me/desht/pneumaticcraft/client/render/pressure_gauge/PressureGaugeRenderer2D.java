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

package me.desht.pneumaticcraft.client.render.pressure_gauge;

import me.desht.pneumaticcraft.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * 2D (GUI) pressure gauge. 26.1 port: the original drew with raw GL vertex buffers; this version draws the gauge
 * with GUI fills (pixel-sized quads), which is plenty for a 40x40 pixel dial.
 */
public class PressureGaugeRenderer2D {
    static final int CIRCLE_POINTS = 500;
    static final float RADIUS = 20F;
    private static final float PI_F = (float) Math.PI;
    private static final float START_ANGLE = 240F / 180F * PI_F;
    static final float STOP_ANGLE = -60F / 180F * PI_F;
    static final int GAUGE_POINTS = (int) ((START_ANGLE - STOP_ANGLE) / (2F * PI_F) * CIRCLE_POINTS);

    static final int RED = 0xFFB84141;
    static final int GREEN = 0xFF79AD39;
    static final int YELLOW = 0xFFEBA23D;
    static final int HUB = 0xFF808019;

    public static void drawPressureGauge(GuiGraphics graphics, Font fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos) {
        drawPressureGauge(graphics, fontRenderer, minPressure, maxPressure, dangerPressure, minWorkingPressure, currentPressure, xPos, yPos, 0xFF000000);
    }

    public static void drawPressureGauge(GuiGraphics graphics, Font fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos, int fgColor) {
        drawBackground(graphics, minPressure, maxPressure, dangerPressure, minWorkingPressure, xPos, yPos);

        // surround
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            float angle = (float) i / CIRCLE_POINTS * 2F * PI_F;
            plot(graphics, Mth.cos(angle) * RADIUS + xPos, Mth.sin(angle) * RADIUS + yPos, fgColor);
        }

        // scale
        int currentScale = (int) maxPressure;
        List<TextScaler> textScalers = new ArrayList<>();
        for (int i = 0; i <= GAUGE_POINTS; i++) {
            float angle = (float) -i / CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
            if (i == GAUGE_POINTS - (int) ((currentScale - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS)) {
                float x = Mth.cos(angle), y = Mth.sin(angle);
                textScalers.add(new TextScaler(currentScale, (int) (x * RADIUS * 1.25D), (int) (y * RADIUS * 1.25D)));
                currentScale--;
                float r1 = maxPressure > 12 && textScalers.size() % 5 == 1 ? 0.8F : 0.92F;
                float r2 = maxPressure > 12 && textScalers.size() % 5 == 1 ? 1.15F : 1.08F;
                line(graphics, x * RADIUS * r1 + xPos, y * RADIUS * r1 + yPos, x * RADIUS * r2 + xPos, y * RADIUS * r2 + yPos, 0xFF000000);
            }
        }

        // needle
        float angleIndicator0 = GAUGE_POINTS - (int) ((currentPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        float angle = -angleIndicator0 / CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
        float ax = Mth.cos(angle + 0.89F * PI_F) * RADIUS * 0.3F + xPos, ay = Mth.sin(angle + 0.89F * PI_F) * RADIUS * 0.3F + yPos;
        float bx = Mth.cos(angle + 1.11F * PI_F) * RADIUS * 0.3F + xPos, by = Mth.sin(angle + 1.11F * PI_F) * RADIUS * 0.3F + yPos;
        float tx = Mth.cos(angle) * RADIUS * 0.8F + xPos, ty = Mth.sin(angle) * RADIUS * 0.8F + yPos;
        line(graphics, ax, ay, bx, by, fgColor);
        line(graphics, bx, by, tx, ty, fgColor);
        line(graphics, tx, ty, ax, ay, fgColor);

        // numbers
        for (int i = 0; i < textScalers.size(); i++) {
            if (textScalers.size() <= 14 || i % 5 == 0) {
                TextScaler scaler = textScalers.get(i);
                graphics.pose().pushPose();
                graphics.pose().translate(xPos + scaler.x() - 1.5, yPos + scaler.y() - 1.5, 0);
                graphics.pose().scale(0.5f, 0.5f, 1f);
                graphics.drawString(fontRenderer, Integer.toString(scaler.pressure()), 0, 0, fgColor, false);
                graphics.pose().popPose();
            }
        }
    }

    private static void drawBackground(GuiGraphics graphics, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, int xPos, int yPos) {
        int explodeBoundary = GAUGE_POINTS - (int) ((dangerPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        int workingBoundary = GAUGE_POINTS - (int) ((minWorkingPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        boolean vacuumWorking = minWorkingPressure < 0 && minWorkingPressure >= -1;
        int r = (int) RADIUS;
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                if (dx * dx + dy * dy > RADIUS * RADIUS) continue;
                float a = (float) Math.atan2(dy, dx);  // screen space angle
                float t = (PI_F / 3f - a) % (2f * PI_F);
                if (t < 0) t += 2f * PI_F;
                int i = (int) (t / (2f * PI_F) * CIRCLE_POINTS);
                if (i >= GAUGE_POINTS) continue;  // gap at the bottom of the dial
                int color = RED;
                if (i >= explodeBoundary) color = vacuumWorking ? YELLOW : GREEN;
                if (i >= workingBoundary) color = vacuumWorking ? GREEN : YELLOW;
                graphics.fill(xPos + dx, yPos + dy, xPos + dx + 1, yPos + dy + 1, color);
            }
        }
        graphics.fill(xPos, yPos, xPos + 1, yPos + 1, HUB);
    }

    private static void plot(GuiGraphics graphics, float x, float y, int color) {
        int px = Math.round(x), py = Math.round(y);
        graphics.fill(px, py, px + 1, py + 1, color);
    }

    private static void line(GuiGraphics graphics, float x1, float y1, float x2, float y2, int color) {
        int steps = (int) Math.max(1, Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)));
        for (int i = 0; i <= steps; i++) {
            plot(graphics, x1 + (x2 - x1) * i / steps, y1 + (y2 - y1) * i / steps, color);
        }
    }

    record TextScaler(int pressure, int x, int y) {
    }
}
