package rpggods.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Base class for the buttons that draw themselves using texture regions instead of the default sprites.
 * Tooltips are drawn by the owning screen using {@link #getTooltipLines(int, int)}.
 */
public abstract class RGButton extends Button {

    protected RGButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    /** Draws this button, only called when it is visible **/
    protected abstract void draw(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick);

    /** @return the tooltip lines to draw when this button is hovered, or an empty list **/
    public List<Component> getTooltipLines(int mouseX, int mouseY) {
        return List.of();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        draw(g, mouseX, mouseY, partialTick);
    }

    public void moveBy(int dx, int dy) {
        setX(getX() + dx);
        setY(getY() + dy);
    }
}
