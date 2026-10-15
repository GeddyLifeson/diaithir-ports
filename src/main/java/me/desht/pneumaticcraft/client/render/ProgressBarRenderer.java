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

package me.desht.pneumaticcraft.client.render;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.util.LightCoordsUtil;

import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.client.util.TintColor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static me.desht.pneumaticcraft.client.util.RenderUtils.normalLine;
import static net.minecraft.util.LightCoordsUtil.FULL_BRIGHT;

public class ProgressBarRenderer {
    public static void render3d(PoseStack poseStack, MultiBufferSource buffer, float minX, float minY, float maxX, float maxY, float zLevel, float progress, int color1, int color2) {
        Pair<float[], float[]> cols = calcColors(color1, color2, progress);
        float[] f1 = cols.getLeft();
        float[] f2 = cols.getRight();

        float x = Mth.lerp(progress / 100F, minX, maxX);

        // draw the bar
        RenderUtils.renderWithTypeAndFinish(poseStack, buffer, ModRenderTypes.UNTEXTURED_QUAD_NO_DEPTH, (posMat, builder) -> {
            builder.addVertex(posMat, minX, minY, zLevel).setColor(f1[0], f1[1], f1[2], f1[3]).setLight(FULL_BRIGHT);
            builder.addVertex(posMat, minX, minY + (maxY - minY), zLevel).setColor(f1[0], f1[1], f1[2], f1[3]).setLight(FULL_BRIGHT);
            builder.addVertex(posMat, x, minY + (maxY - minY), zLevel).setColor(f2[0], f2[1], f2[2], f2[3]).setLight(FULL_BRIGHT);
            builder.addVertex(posMat, x, minY, zLevel).setColor(f2[0], f2[1], f2[2], f2[3]).setLight(FULL_BRIGHT);
        });

        // draw the outline
        final Matrix3f normal = poseStack.last().normal();
        RenderUtils.renderWithTypeAndFinish(poseStack, buffer, ModRenderTypes.getLineLoops(1.5), (posMat, builder) -> {
            normalLine(builder, poseStack, minX, minY, zLevel, minX, maxY, zLevel, 255,0, 0, 0, true);
            normalLine(builder, poseStack, minX, maxY, zLevel, maxX, maxY, zLevel, 255,0, 0, 0, true);
            normalLine(builder, poseStack, maxX, maxY, zLevel, maxX, minY, zLevel, 255,0, 0, 0, true);
            normalLine(builder, poseStack, maxX, minY, zLevel, minX, minY, zLevel, 255,0, 0, 0, true);
        });
    }

    public static void render2d(GuiGraphicsExtractor graphics, float minX, float minY, float maxX, float maxY, float zLevel, float progress, int color1, int color2) {
        Pair<float[], float[]> cols = calcColors(color1, color2, progress);
        float[] f1 = cols.getLeft();
        float[] f2 = cols.getRight();

        float x = Mth.lerp(progress / 100F, minX, maxX);

        // 26.1 GUIs have no immediate-mode vertex drawing: the gradient is drawn as 1px wide columns
        int ix0 = Math.round(minX), ix1 = Math.round(x), iy0 = Math.round(minY), iy1 = Math.round(maxY);
        for (int cx = ix0; cx < ix1; cx++) {
            float t = ix1 == ix0 ? 0f : (float) (cx - ix0) / (ix1 - ix0);
            int col = argb(Mth.lerp(t, f1[3], f2[3]), Mth.lerp(t, f1[0], f2[0]), Mth.lerp(t, f1[1], f2[1]), Mth.lerp(t, f1[2], f2[2]));
            graphics.fill(cx, iy0, cx + 1, iy1, col);
        }
        int ox1 = Math.round(maxX);
        graphics.fill(ix0, iy0, ox1, iy0 + 1, 0xFF000000);
        graphics.fill(ix0, iy1 - 1, ox1, iy1, 0xFF000000);
        graphics.fill(ix0, iy0, ix0 + 1, iy1, 0xFF000000);
        graphics.fill(ox1 - 1, iy0, ox1, iy1, 0xFF000000);
    }

    private static int argb(float a, float r, float g, float b) {
        return (int) (a * 255) << 24 | (int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
    }

    private static Pair<float[], float[]> calcColors(int color1, int color2, float progress) {
        float[] f1 = new TintColor(color1, true).getComponents(null);
        float[] f2;
        if (color1 != color2) {
            f2 = new TintColor(color2, true).getComponents(null);
            for (int i = 0; i < f1.length; i++) {
                f2[i] = Mth.lerp(progress / 100f, f1[i], f2[i]);
            }
            return Pair.of(f1, f2);
        } else {
            return Pair.of(f1, f1);
        }
    }
}
