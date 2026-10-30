package com.github.alexmodguy.alexscaves.client.gui.book.widget;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class EntityBoxWidget extends EntityWidget {

    @Expose
    @SerializedName("box_width")
    private float boxWidth;
    @Expose
    @SerializedName("box_height")
    private float boxHeight;
    @Expose
    @SerializedName("box_scale")
    private float boxScale;
    @Expose
    @SerializedName("entity_x_offset")
    private float entityXOffset;
    @Expose
    @SerializedName("entity_y_offset")
    private float entityYOffset;
    @Expose
    @SerializedName("box_image")
    private String borderImage;

    private transient Identifier borderTexture;

    private static final int TEX = 64;
    private static final int C = 10;
    private static final int L = 4;

    public EntityBoxWidget(int displayPage, String entityId, boolean sepia, float boxWidth, float boxHeight, float boxScale, float entityXOffset, float entityYOffset, String borderImage, String entityNBT, int x, int y, float scale) {
        super(displayPage, Type.ENTITY_BOX, entityId, sepia, entityNBT, x, y, scale);
        this.boxHeight = boxHeight;
        this.boxWidth = boxWidth;
        this.borderImage = borderImage;
        this.boxScale = boxScale;
        this.entityXOffset = entityXOffset;
        this.entityYOffset = entityYOffset;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, PageContext ctx, float partialTicks) {
        if (this.borderTexture == null) {
            this.borderTexture = Identifier.parse(borderImage);
        }
        int tint = isSepia() ? SEPIA_TINT : -1;
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(getX() - boxWidth * 0.5F * boxScale, getY() - boxHeight * 0.5F * boxScale);
        pose.scale(boxScale, boxScale);
        int w = Math.round(boxWidth);
        int h = Math.round(boxHeight);
        int h2 = C / 2;
        int e = C + L;
        int e2 = e + C;
        // corners, centred on the box corners
        piece(graphics, -h2, -h2, C, C, 0, 0, C, C, tint);
        piece(graphics, w - h2, -h2, C, C, e, 0, C, C, tint);
        piece(graphics, -h2, h - h2, C, C, 0, e, C, C, tint);
        piece(graphics, w - h2, h - h2, C, C, e, e, C, C, tint);
        // edges
        piece(graphics, h2, -h2, w - C, C, C, 0, L, C, tint);
        piece(graphics, h2, h - h2, w - C, C, C, e, L, C, tint);
        piece(graphics, -h2, h2, C, h - C, 0, C, C, L, tint);
        piece(graphics, w - h2, h2, C, h - C, e, C, C, L, tint);
        pose.popMatrix();

        renderEntityAt(graphics, ctx, getX(), getY(), entityXOffset * getScale(), entityYOffset * getScale());
    }

    private void piece(GuiGraphicsExtractor g, int x, int y, int w, int h, int u, int v, int sw, int sh, int tint) {
        if (w <= 0 || h <= 0) {
            return;
        }
        g.blit(RenderPipelines.GUI_TEXTURED, borderTexture, x, y, u, v, w, h, sw, sh, TEX, TEX, tint);
    }
}
