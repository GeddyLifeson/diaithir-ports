package dev.shadowsoffire.apothic_enchanting.table;

import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

/**
 * {@link GuiElementRenderState} sibling of vanilla's {@code BlitRenderState} that stores screen
 * coordinates as floats so the resulting quad can land at sub-pixel positions and have a
 * sub-pixel width. The texture coordinates and color follow the standard convention.
 * <p>
 * Used by {@link RavenEnchantmentScreen} to overdraw the integer-precision stat bars with
 * fractional-width fills so the bar tip stays aligned with the floating handle as the player
 * drags — eliminating the 1-pixel jitter that the parent's int rounding introduces.
 * <p>
 * Submitted via {@link GuiGraphicsExtractor#submitGuiElementRenderState(GuiElementRenderState)}
 * (a NeoForge extension on the public surface, no AT needed).
 */
public record FractionalBlitRenderState(
    RenderPipeline pipeline,
    TextureSetup textureSetup,
    Matrix3x2f pose,
    float x0, float y0, float x1, float y1,
    float u0, float u1, float v0, float v1,
    int color,
    @Nullable ScreenRectangle bounds) implements GuiElementRenderState {

    /**
     * Builds a fractional blit. {@code srcW/srcH} are the source rect's pixel dimensions on the
     * atlas; {@code texW/texH} are the atlas dimensions (typically 256). Note {@code w/h} can
     * carry fractional pixels; the source rect is sampled proportionally.
     */
    public static FractionalBlitRenderState of(GuiGraphicsExtractor gfx, Identifier texture,
        float x, float y, float w, float h,
        float u, float v, float srcW, float srcH,
        float texW, float texH) {
        AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(texture);
        return new FractionalBlitRenderState(
            RenderPipelines.GUI_TEXTURED,
            TextureSetup.singleTexture(tex.getTextureView(), tex.getSampler()),
            new Matrix3x2f(gfx.pose()),
            x, y, x + w, y + h,
            u / texW, (u + srcW) / texW,
            v / texH, (v + srcH) / texH,
            -1,
            new ScreenRectangle(
                (int) Math.floor(x),
                (int) Math.floor(y),
                (int) Math.ceil(w),
                (int) Math.ceil(h)));
    }

    @Override
    public void buildVertices(VertexConsumer vc) {
        vc.addVertexWith2DPose(this.pose, this.x0, this.y0).setUv(this.u0, this.v0).setColor(this.color);
        vc.addVertexWith2DPose(this.pose, this.x0, this.y1).setUv(this.u0, this.v1).setColor(this.color);
        vc.addVertexWith2DPose(this.pose, this.x1, this.y1).setUv(this.u1, this.v1).setColor(this.color);
        vc.addVertexWith2DPose(this.pose, this.x1, this.y0).setUv(this.u1, this.v0).setColor(this.color);
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }

}
