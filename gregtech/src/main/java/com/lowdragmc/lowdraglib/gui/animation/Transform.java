package com.lowdragmc.lowdraglib.gui.animation;

import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.lowdragmc.lowdraglib.utils.interpolate.IEase;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2022/9/8
 * @implNote MoveIn
 */
@Accessors(chain = true, fluent = true)
public class Transform extends Animation {
    protected int xOffset, yOffset;
    @Setter
    protected float scale = 1;
    protected boolean in;

    public Transform offset(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        return this;
    }

    public Transform setScale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public Transform duration(long duration) { return null; /* ported stub */ }

    @Override
    public Transform delay(long delay) { return null; /* ported stub */ }

    @Override
    public Transform ease(IEase ease) { return null; /* ported stub */ }

    @Override
    public Transform onUpdate(FloatConsumer onUpdate) { return null; /* ported stub */ }

    @Override
    public Animation onFinish(Runnable onFinish) { return null; /* ported stub */ }

    @Override
    public Animation size(@Nullable Size size) { return null; /* ported stub */ }

    @Override
    public Animation position(@Nullable Position position) { return null; /* ported stub */ }

    public boolean isIn() {
        return in;
    }

    public boolean isOut() {
        return !in;
    }

    public Animation setIn() {
        this.in = true;
        return this;
    }

    public Animation setOut() {
        this.in = false;
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public void pre(@NotNull GuiGraphics graphics) {  /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    public void post(@NotNull GuiGraphics graphics) {
        graphics.pose().popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        float tickTime = getTick();
        if (tickTime >= delay) {
            if (interpolator != null) {
                interpolator.update(tickTime);
            }
            pre(graphics);
            widget.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            post(graphics);
        } else if (isOut()) {
            widget.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        float tickTime = getTick();
        if (tickTime >= delay) {
            if (interpolator != null) {
                interpolator.update(tickTime);
            }
            pre(graphics);
            widget.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            post(graphics);
        } else if (isOut()) {
            widget.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        }
    }
}
