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

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 26.1 builds render types from pipelines. These are small pipelines on vanilla's position/colour shaders, so code that
 * writes position+colour(+uv) vertices keeps working: extra vertex elements it writes (light, normal) are skipped.
 * Dropped: lightmap lighting on the textured types, and line width (debug lines are 1px).
 */
public class ModRenderTypes {
    private static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath("pneumaticcraft", "pipeline/" + path);
    }

    private static RenderPipeline.Builder colorBase(String name, VertexFormat.Mode mode, boolean depth) {
        return RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(loc(name)).withCull(false)
                .withDepthStencilState(depth ? Optional.of(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false)) : Optional.empty())
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, mode);
    }

    private static RenderType color(String name, VertexFormat.Mode mode, boolean depth) {
        return RenderType.create("pnc_" + name, RenderSetup.builder(colorBase(name, mode, depth).build()).createRenderSetup());
    }

    private static final RenderType QUADS_NO_DEPTH = color("quads_no_depth", VertexFormat.Mode.QUADS, false);
    private static final RenderType QUADS_DEPTH = color("quads_depth", VertexFormat.Mode.QUADS, true);
    private static final RenderType LINES_NO_DEPTH = color("lines_no_depth", VertexFormat.Mode.DEBUG_LINES, false);
    private static final RenderType LINES_DEPTH = color("lines_depth", VertexFormat.Mode.DEBUG_LINES, true);
    private static final RenderType STRIP_NO_DEPTH = color("line_strip_no_depth", VertexFormat.Mode.DEBUG_LINE_STRIP, false);

    private static final Map<Identifier, RenderType> TEXTURED = new ConcurrentHashMap<>();
    private static final Map<Identifier, RenderType> TEXTURED_NO_DEPTH = new ConcurrentHashMap<>();

    private static RenderType textured(Identifier texture, boolean depth) {
        return (depth ? TEXTURED : TEXTURED_NO_DEPTH).computeIfAbsent(texture, t -> RenderType.create("pnc_texture_" + t,
                RenderSetup.builder(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                                .withLocation(loc(depth ? "textured" : "textured_no_depth")).withCull(false)
                                .withDepthStencilState(depth ? Optional.of(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true)) : Optional.empty())
                                .build())
                        .withTexture("Sampler0", t).createRenderSetup()));
    }

    public static RenderType getTextureRender(Identifier texture) {
        return textured(texture, true);
    }

    public static RenderType getTextureRenderColored(Identifier texture) {
        return textured(texture, true);
    }

    public static RenderType getTextureRenderColored(Identifier texture, boolean disableDepthTest) {
        return textured(texture, !disableDepthTest);
    }

    public static final RenderType UNTEXTURED_QUAD_NO_DEPTH = QUADS_NO_DEPTH;

    public static final RenderType UNTEXTURED_QUAD = QUADS_DEPTH;

    public static final RenderType BLOCK_FRAME = QUADS_NO_DEPTH;

    public static final RenderType BLOCK_TRACKER = LINES_NO_DEPTH;

    public static final RenderType TARGET_CIRCLE = RenderType.create("pnc_target_circle", RenderSetup.builder(
            colorBase("tri_strip_no_depth", VertexFormat.Mode.TRIANGLE_STRIP, false).build()).createRenderSetup());

    /** Vertices are a continuous line strip. The width argument is ignored. */
    public static RenderType getLineLoops(double lineWidth) {
        return STRIP_NO_DEPTH;
    }

    public static RenderType getBlockHilightFace(boolean disableDepthTest, boolean disableWriteMask) {
        return disableDepthTest ? QUADS_NO_DEPTH : QUADS_DEPTH;
    }

    public static RenderType getBlockHilightLine(boolean disableDepthTest, boolean disableWriteMask) {
        return disableDepthTest ? LINES_NO_DEPTH : LINES_DEPTH;
    }

    public static final RenderType TRIANGLE_FAN = RenderTypes.debugTriangleFan();

    public static final RenderType TRIANGLES = color("triangles", VertexFormat.Mode.TRIANGLES, true);

    public static RenderType getArmorTranslucentNoCull(Identifier rl) {
        return RenderTypes.armorTranslucent(rl);
    }
}
