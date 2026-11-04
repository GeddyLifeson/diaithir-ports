package io.github.tr100000.trutils.api.gui;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public class ScaledItemGuiElementRenderState extends GuiItemRenderState {
    private final Matrix3x2f pose;
    private final TrackingItemStackRenderState state;
    private final int x;
    private final int y;
    private final int size;
    @Nullable private final ScreenRectangle scissorArea;
    @Nullable private final ScreenRectangle oversizedBounds;
    @Nullable private final ScreenRectangle bounds;

    public ScaledItemGuiElementRenderState(Matrix3x2f pose, TrackingItemStackRenderState state, int x, int y, int size, @Nullable ScreenRectangle scissor) {
        super(pose, state, x, y, scissor);
        final float scale = size / 16.0F;
        this.pose = pose.scaleAround(scale, x, y);
        this.state = state;
        this.x = x;
        this.y = y;
        this.size = size;
        this.scissorArea = scissor;
        this.oversizedBounds = state.isOversizedInGui() ? createScaleldOversizedBounds() : null;
        this.bounds = createScaledBounds(oversizedBounds != null ? oversizedBounds : new ScreenRectangle(x, y, size, size));
    }

    @Nullable private ScreenRectangle createScaleldOversizedBounds() {
        AABB box = this.state.getModelBoundingBox();
        int i = Mth.ceil(box.getXsize() * size);
        int j = Mth.ceil(box.getYsize() * size);
        if (i <= size && j <= size) {
            return null;
        } else {
            float f = (float)(box.minX * size);
            float g = (float)(box.maxY * size);
            int k = Mth.floor(f);
            int l = Mth.floor(g);
            int m = this.x + k + (size / 2);
            int n = this.y - l + (size / 2);
            return new ScreenRectangle(m, n, i, j);
        }
    }

    @Nullable private ScreenRectangle createScaledBounds(ScreenRectangle screenRect) {
        ScreenRectangle screenRect2 = screenRect.transformMaxBounds(this.pose);
        return this.scissorArea != null ? this.scissorArea.intersection(screenRect2) : screenRect2;
    }

    @Override
    @Nullable public ScreenRectangle oversizedItemBounds() {
        return this.oversizedBounds;
    }

    @Override
    @Nullable public ScreenRectangle bounds() {
        return this.bounds;
    }
}
