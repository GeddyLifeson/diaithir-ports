package com.github.alexmodguy.alexscaves.client.gui.book.widget;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class ImageWidget extends BookWidget {

    @Expose
    @SerializedName("image")
    private String image;
    @Expose
    private boolean sepia;
    @Expose
    private int u0;
    @Expose
    private int v0;
    @Expose
    private int u1;
    @Expose
    private int v1;
    @Expose
    private int width;
    @Expose
    private int height;

    private transient Identifier actualTexture;

    public ImageWidget(int displayPage, String image, boolean sepia, int u0, int v0, int u1, int v1, int width, int height, int x, int y, float scale) {
        super(displayPage, Type.IMAGE, x, y, scale);
        this.image = image;
        this.sepia = sepia;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, PageContext ctx, float partialTicks) {
        if (this.actualTexture == null) {
            this.actualTexture = Identifier.parse(image);
        }
        // the upstream quad is centred on (x, y) and (u1-u0) texels wide at the widget scale
        float w = (u1 - u0) * getScale();
        float h = (v1 - v0) * getScale();
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(getX() - w * 0.5F, getY() - h * 0.5F);
        pose.scale(getScale(), getScale());
        graphics.blit(RenderPipelines.GUI_TEXTURED, actualTexture, 0, 0, u0, v0, u1 - u0, v1 - v0, u1 - u0, v1 - v0, width, height, sepia ? SEPIA_TINT : -1);
        pose.popMatrix();
    }
}
