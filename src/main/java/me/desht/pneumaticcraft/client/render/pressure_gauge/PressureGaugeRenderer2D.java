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

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class PressureGaugeRenderer2D {
    static final int CIRCLE_POINTS = 500;
    static final float RADIUS = 20F;
    private static final float PI_F = (float) Math.PI;
    private static final float START_ANGLE = 240F / 180F * PI_F;
    static final float STOP_ANGLE = -60F / 180F * PI_F;
    static final int GAUGE_POINTS = (int) ((START_ANGLE - STOP_ANGLE) / (2F * PI_F) * CIRCLE_POINTS);

    static final float[] RED = new float[] { 0.722f, 0.255f, 0.255f, 1f };
    static final float[] GREEN = new float[] { 0.475f, 0.678f, 0.224f, 1f };
    static final float[] YELLOW = new float[] { 0.922f, 0.635f, 0.239f, 1f };

    public static void drawPressureGauge(GuiGraphicsExtractor graphics, Font fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos) {
        drawPressureGauge(graphics, fontRenderer, minPressure, maxPressure, dangerPressure, minWorkingPressure, currentPressure, xPos, yPos, 0xFF000000);
    }

    /**
     * Render a pressure gauge into a GUI.  Do not use for in-world rendering (e.g. pressure gauge module)
     *
     * @param fontRenderer the font renderer
     * @param minPressure minimum pressure
     * @param maxPressure maximum pressure
     * @param dangerPressure danger pressure (red area)
     * @param minWorkingPressure min. working pressure
     * @param currentPressure current pressure (where needle points)
     * @param xPos x position
     * @param yPos y position
     * @param fgColor color to draw the surround, needle and text
     */
    public static void drawPressureGauge(GuiGraphicsExtractor graphics, Font fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos, int fgColor) {
        // 26.1 GUIs have no immediate-mode vertex drawing: the gauge is plotted as 1px fills
        drawGaugeBackground(graphics, minPressure, maxPressure, dangerPressure, minWorkingPressure, xPos, yPos);
        drawGaugeSurround(graphics, xPos, yPos, fgColor);

        int currentScale = (int) maxPressure;
        List<TextScaler> textScalers = new ArrayList<>();
        drawScale(graphics, minPressure, maxPressure, xPos, yPos, currentScale, textScalers);

        float angleIndicator0 = GAUGE_POINTS - (int) ((currentPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        float angleIndicator = -angleIndicator0 / CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
        drawNeedle(graphics, xPos, yPos, angleIndicator, fgColor);

        drawText(graphics, fontRenderer, xPos, yPos, fgColor, textScalers);
    }

    private static int argb(float[] c) {
        return (int) (c[3] * 255) << 24 | (int) (c[0] * 255) << 16 | (int) (c[1] * 255) << 8 | (int) (c[2] * 255);
    }

    private static void line(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, int color) {
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1))));
        for (int i = 0; i <= steps; i++) {
            int x = Math.round(Mth.lerp((float) i / steps, x1, x2));
            int y = Math.round(Mth.lerp((float) i / steps, y1, y2));
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private static void drawNeedle(GuiGraphicsExtractor graphics, int xPos, int yPos, float angle, int fgColor) {
        float x1 = Mth.cos(angle + 0.89F * PI_F) * RADIUS * 0.3F + xPos;
        float y1 = Mth.sin(angle + 0.89F * PI_F) * RADIUS * 0.3F + yPos;
        float x2 = Mth.cos(angle + 1.11F * PI_F) * RADIUS * 0.3F + xPos;
        float y2 = Mth.sin(angle + 1.11F * PI_F) * RADIUS * 0.3F + yPos;
        float x3 = Mth.cos(angle) * RADIUS * 0.8F + xPos;
        float y3 = Mth.sin(angle) * RADIUS * 0.8F + yPos;
        line(graphics, x1, y1, x2, y2, fgColor);
        line(graphics, x2, y2, x3, y3, fgColor);
        line(graphics, x3, y3, x1, y1, fgColor);
    }

    private static void drawScale(GuiGraphicsExtractor graphics, float minPressure, float maxPressure, int xPos, int yPos, int currentScale, List<TextScaler> textScalers) {
        for (int i = 0; i <= GAUGE_POINTS; i++) {
            float angle = (float) -i / (float) CIRCLE_POINTS * 2F * PI_F - STOP_ANGLE;
            if (i == GAUGE_POINTS - (int) ((currentScale - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS)) {
                float x = Mth.cos(angle);
                float y = Mth.sin(angle);
                textScalers.add(new TextScaler(currentScale, (int) (x * RADIUS * 1.25D), (int) (y * RADIUS * 1.25D)));
                currentScale--;
                float r1 = maxPressure > 12 && textScalers.size() % 5 == 1 ? 0.8F : 0.92F;
                float r2 = maxPressure > 12 && textScalers.size() % 5 == 1 ? 1.15F : 1.08F;
                line(graphics, x * RADIUS * r1 + xPos, y * RADIUS * r1 + yPos, x * RADIUS * r2 + xPos, y * RADIUS * r2 + yPos, 0xFF000000);
            }
        }
    }

    private static void drawText(GuiGraphicsExtractor graphics, Font fontRenderer, int xPos, int yPos, int fgColor, List<TextScaler> textScalers) {
        for (int i = 0; i < textScalers.size(); i++) {
            if (textScalers.size() <= 14 || i % 5 == 0) {
                TextScaler scaler = textScalers.get(i);
                graphics.pose().pushMatrix();
                graphics.pose().translate(xPos + scaler.x() - 1.5f, yPos + scaler.y() - 1.5f);
                graphics.pose().scale(0.5f, 0.5f);
                graphics.text(fontRenderer, Integer.toString(scaler.pressure()), 0, 0, fgColor, false);
                graphics.pose().popMatrix();
            }
        }
    }

    private static void drawGaugeBackground(GuiGraphicsExtractor graphics, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, int xPos, int yPos) {
        int explodeBoundary = GAUGE_POINTS - (int) ((dangerPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        int workingBoundary = GAUGE_POINTS - (int) ((minWorkingPressure - minPressure) / (maxPressure - minPressure) * GAUGE_POINTS);
        boolean swap = minWorkingPressure < 0 && minWorkingPressure >= -1;
        int afterExplode = argb(swap ? YELLOW : GREEN);
        int afterWorking = argb(swap ? GREEN : YELLOW);
        int red = argb(RED);
        int r = (int) RADIUS;
        for (int dy = -r; dy <= r; dy++) {
            int runStart = 0, runColor = 0;
            boolean inRun = false;
            for (int dx = -r; dx <= r + 1; dx++) {
                int color = 0;
                if (dx <= r && dx * dx + dy * dy <= RADIUS * RADIUS) {
                    float i = (PI_F / 3F - (float) Math.atan2(dy, dx)) * CIRCLE_POINTS / (2F * PI_F);
                    if (i < 0) i += CIRCLE_POINTS;
                    if (i <= GAUGE_POINTS) {
                        color = red;
                        if (explodeBoundary <= workingBoundary) {
                            if (i >= explodeBoundary) color = afterExplode;
                            if (i >= workingBoundary) color = afterWorking;
                        } else {
                            if (i >= workingBoundary) color = afterWorking;
                            if (i >= explodeBoundary) color = afterExplode;
                        }
                    }
                }
                if (inRun && color != runColor) {
                    graphics.fill(xPos + runStart, yPos + dy, xPos + dx, yPos + dy + 1, runColor);
                    inRun = false;
                }
                if (!inRun && color != 0) {
                    inRun = true;
                    runStart = dx;
                    runColor = color;
                }
            }
        }
    }

    private static void drawGaugeSurround(GuiGraphicsExtractor graphics, int xPos, int yPos, int fgColor) {
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            float angle = (float) i / (float) CIRCLE_POINTS * 2F * PI_F;
            int x = Math.round(Mth.cos(angle) * RADIUS + xPos);
            int y = Math.round(Mth.sin(angle) * RADIUS + yPos);
            graphics.fill(x, y, x + 1, y + 1, fgColor);
        }
    }

    record TextScaler(int pressure, int x, int y) {
    }
}
