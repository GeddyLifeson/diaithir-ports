package io.github.tr100000.trutils.api.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public abstract class AbstractView extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
    private final List<GuiEventListener> children = new ObjectArrayList<>();
    private final List<Renderable> drawables = new ObjectArrayList<>();

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        drawables.forEach(drawable -> drawable.extractRenderState(graphics, mouseX, mouseY, delta));
    }

    public void clearChildren() {
        children.clear();
        drawables.clear();
    }

    public <T extends GuiEventListener> T addChild(T child) {
        children.add(child);
        return child;
    }

    public <T extends GuiEventListener & Renderable> T addDrawableChild(T child) {
        children.add(child);
        drawables.add(child);
        return child;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder) {}

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }
}
