package io.github.tr100000.researcher.screen;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.ResearchProgress;
import io.github.tr100000.trutils.api.gui.GuiHelper;
import io.github.tr100000.trutils.api.gui.IconRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.util.CommonColors;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class ResearchNodeWidget extends AbstractButton {
    private static final Minecraft client = Minecraft.getInstance();
    private final ClientTooltipPositioner tooltipPositioner = new TooltipPositionerImpl();
    private final ResearchTooltipWrapper tooltipWrapper = new ResearchTooltipWrapper(true);
    private final ResearchScreen screen;
    private final ResearchNodeContainingView parentView;

    public final Research research;

    public static final int FILL_LOCKED = 0xFFB82121;
    public static final int FILL_AVAILABLE = 0xFFBABABA;
    public static final int FILL_FINISHED = 0xFF149900;
    public static final int FILL_PROGRESS_BACKGROUND = CommonColors.GRAY;
    public static final int FILL_PROGRESS_BAR = 0xFF28C900;

    public ResearchNodeWidget(ResearchScreen screen, ResearchNodeContainingView parentView, int x, int y, int width, int height, Research research) {
        super(x, y, width, height, research.getTitle(client.getConnection().researcher$getClientTracker()));
        this.screen = screen;
        this.parentView = parentView;
        this.research = research;
    }

    public ResearchNodeWidget(ResearchScreen screen, ResearchNodeContainingView parentView, int x, int y, int width, int height, int depth, Research research) {
        this(screen, parentView, x, y, width, height, research);
    }

    @Override
    public void extractContents(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        ResearchProgress progress = screen.researchManager.getProgress(research);
        boolean showProgressBar = !progress.isFinished() && progress.getCount() > 0;
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), getFillColor(progress));

        int iconSize = (int)(getWidth() * 0.85F);
        if (iconSize % 2 == 1) iconSize++;
        int iconOffset = (getWidth() - iconSize) / 2;
        IconRenderers.drawWithSize(research.display(), graphics, getX() + iconOffset, getY() + iconOffset - (showProgressBar ? 1 : 0), iconSize, delta);
        if (showProgressBar) {
            graphics.fill(getX(), getY() + getHeight() - 2, getX() + getWidth(), getY() + getHeight(), FILL_PROGRESS_BACKGROUND);
            graphics.fill(getX(), getY() + getHeight() - 2, getX() + progress.getScaledProgress(research.trigger().count(), getWidth()), getY() + getHeight(), FILL_PROGRESS_BAR);
        }

        double actualMouseX = parentView.offsetToScreenX(mouseX);
        double actualMouseY = parentView.offsetToScreenY(mouseY);
        if (graphics.containsPointInScissor((int)actualMouseX, (int)actualMouseY) && isMouseOver(mouseX, mouseY)) {
            GuiHelper.slotHighlight(graphics, this);
            GuiHelper.tooltip(graphics, client.font, tooltipWrapper.getOrCreate(research), mouseX, mouseY, tooltipPositioner);
            graphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    private int getFillColor(ResearchProgress progress) {
        if (progress.isFinished()) return FILL_FINISHED;
        return screen.researchManager.canResearch(research) ? FILL_AVAILABLE : FILL_LOCKED;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return parentView.isMouseOver(parentView.offsetToScreenX(mouseX), parentView.offsetToScreenY(mouseY)) && super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (input.hasShiftDown()) {
            parentView.onNodeShiftClicked(this);
        }
        else {
            screen.initWith(research);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        defaultButtonNarrationText(builder);
    }

    /**
     * Special implementation of {@link ClientTooltipPositioner} to take view offsets into account.
     * @implNote Modified version of {@link net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner}.
     */
    private class TooltipPositionerImpl implements ClientTooltipPositioner {
        @Override
        public Vector2ic positionTooltip(int screenWidth, int screenHeight, int x, int y, int width, int height) {
            Vector2i worldVec = new Vector2i(x, y).add(12, -12);
            Vector2i result = new Vector2i((int)parentView.offsetToScreenX(worldVec.x()), (int)parentView.offsetToScreenY(worldVec.y()));
            this.preventOverflow(screenWidth, screenHeight, result, width, height);
            return result;
        }

        private void preventOverflow(int screenWidth, int screenHeight, Vector2i pos, int width, int height) {
            if (pos.x + width > screenWidth) {
                pos.x = Math.max(pos.x - 24 - width, 4);
            }

            int i = height + 3;
            if (pos.y + i > screenHeight) {
                pos.y = screenHeight - i;
            }
        }
    }
}
