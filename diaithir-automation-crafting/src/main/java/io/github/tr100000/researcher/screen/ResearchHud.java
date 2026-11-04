package io.github.tr100000.researcher.screen;

import io.github.tr100000.researcher.ClientResearchTracker;
import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.ResearchProgress;
import io.github.tr100000.researcher.config.ResearcherConfigs;
import io.github.tr100000.trutils.api.gui.GuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

public final class ResearchHud {
    private ResearchHud() {}

    public static final Identifier LAYER_ID = ModUtils.id("hud");

    private static final Minecraft client = Minecraft.getInstance();
    private static int percentageTextMaxWidth;

    private static final int BACKGROUND_COLOR = 0xFF202020;
    private static final int BORDER_COLOR = 0xFF000000;
    private static final int BORDER_COLOR_HOVERED = 0xFFFF0000;
    private static final int PROGRESS_BACKGROUND_COLOR = 0xFF707070;
    private static final int PROGRESS_FILL_COLOR = 0xFF28C900;

    public static void render(final GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        if (client.level == null || client.player == null || !ResearcherConfigs.client.researchHud.get()) return;
        if (client.debugEntries.isOverlayVisible()) return; // Don't show when F3 is open
        if (client.screen instanceof ResearchScreen) return; // Don't show with research screen open (it doesn't render properly)
        if (client.getConnection() == null) return;

        ClientResearchTracker researchTracker = client.getConnection().researcher$getClientTracker();
        if (researchTracker == null) return;

        if (percentageTextMaxWidth <= 0) percentageTextMaxWidth = client.font.width("99%");

        int y = 0;

        if (researchTracker.getCurrentResearching() != null) {
            y += render(graphics, researchTracker, researchTracker.getCurrentResearching(), 0);
        }
        for (Identifier researchId : researchTracker.getPinnedResearches()) {
            y += render(graphics, researchTracker, researchTracker.get(researchId), y);
        }
    }

    private static int render(final GuiGraphicsExtractor graphics, ClientResearchTracker researchTracker, @Nullable Research research, int y) {
        if (research == null) return 0;

        boolean hovered = currentScreenCanInteractWithHud() && GuiHelper.isMouseTouching(0, y + 1, 150, 26);

        graphics.fill(0, y, 150, y + 28, BACKGROUND_COLOR);
        graphics.outline(0, y, 150, 28, hovered ? BORDER_COLOR_HOVERED : BORDER_COLOR);
        graphics.text(client.font, research.getTitle(researchTracker), 4, y + 4, CommonColors.WHITE, true);

        renderProgressText(graphics, researchTracker, research, getProgressPercentage(researchTracker, research), y);

        int scaledProgress = getScaledProgress(researchTracker, research, 138 - percentageTextMaxWidth);
        graphics.fill(4, y + 18, 142 - percentageTextMaxWidth, y + 22, PROGRESS_BACKGROUND_COLOR);
        graphics.fill(4, y + 18, 4 + scaledProgress, y + 22, PROGRESS_FILL_COLOR);

        return 27;
    }

    private static void renderProgressText(final GuiGraphicsExtractor graphics, ClientResearchTracker researchTracker, Research research, int progressPercentage, int yPos) {
        final int x = 146 - percentageTextMaxWidth;
        final int y = yPos + 16;

        Component progressText = Component.literal(String.format("%s%%", progressPercentage));
        graphics.text(client.font, progressText, x, y, CommonColors.WHITE, true);

        if (currentScreenCanInteractWithHud() && GuiHelper.isMouseTouching(x, y, percentageTextMaxWidth, 9)) {
            graphics.setTooltipForNextFrame(getHoveredProgressTooltip(researchTracker, research), GuiHelper.getMouseX(), GuiHelper.getMouseY());
        }
    }

    private static int getScaledProgress(ClientResearchTracker researchTracker, Research research, int width) {
        ResearchProgress progress = researchTracker.getProgress(research);
        if (progress.isFinished()) return width;
        int count = progress.getCount();
        int max = research.trigger().count();
        return (int)((float)count / max * width);
    }

    private static int getProgressPercentage(ClientResearchTracker researchTracker, Research research) {
        return getScaledProgress(researchTracker, research, 100);
    }

    private static Component getHoveredProgressTooltip(ClientResearchTracker researchTracker, Research research) {
        ResearchProgress progress = researchTracker.getProgress(research);
        return Component.literal(String.valueOf(progress.getCount())).append(Component.literal(" / ").append(String.valueOf(research.trigger().count())).withStyle(ChatFormatting.DARK_GRAY));
    }

    private static boolean currentScreenCanInteractWithHud() {
        return client.screen instanceof ChatScreen;
    }
}
