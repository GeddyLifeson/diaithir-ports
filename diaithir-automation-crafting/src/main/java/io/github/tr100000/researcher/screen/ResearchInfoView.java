package io.github.tr100000.researcher.screen;

import io.github.tr100000.researcher.ClientResearchTracker;
import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.ResearchCriterion;
import io.github.tr100000.researcher.ResearchProgress;
import io.github.tr100000.researcher.api.ResearchReward;
import io.github.tr100000.researcher.api.trigger.TriggerDisplayElement;
import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.TriggerHandlerRegistry;
import io.github.tr100000.researcher.config.ResearcherConfigs;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

public class ResearchInfoView extends AbstractResearchView {
    private static final int BACKGROUND_COLOR = 0xFF202020;
    private static final int BORDER_COLOR = 0xFF000000;
    private static final int CRITERION_BACKGROUND = 0xFF555555;
    private static final int CRITERION_BORDER = 0xFFABABAB;
    private static final int CRITERION_PROGRESS = 0xFF149900;

    private @Nullable ClientResearchTracker researchTracker;

    private @Nullable Research research;
    private @Nullable TriggerDisplayElement currentDisplay;

    private boolean isScrollable;
    private MinMaxBounds.Ints scrollBounds = MinMaxBounds.Ints.ANY;
    private double offsetY;

    public ResearchInfoView(ResearchScreen parent) {
        super(parent, 0, 0, ResearchScreen.getSidebarWidth(), ResearchScreen.getInfoViewHeight());
    }

    public void initWith(Research research) {
        this.research = research;
        onResize();

        currentDisplay = prepareDisplay(research.trigger());
    }

    @Override
    public void onResize() {
        this.width = ResearchScreen.getSidebarWidth();
        this.height = ResearchScreen.getInfoViewHeight();
        this.scissorRect = new ScreenRectangle(0, 0, width, height);

        assert research != null;
        clearChildren();

        researchTracker = client.getConnection().researcher$getClientTracker();
        boolean isResearchable = researchTracker.getResearchConditions(research) != null && !researchTracker.hasFinished(research) && researchTracker.canResearch(research);

        int y = 42;

        if (!research.recipeUnlocks().isEmpty()) {
            addDrawableChild(new StringWidget(12, y, ResearchScreen.getSidebarWidth() - 24, 10, ModUtils.getScreenTranslated("unlocks"), client.font));
            y += 10;
            int x = 14;
            for (Identifier unlock : research.recipeUnlocks()) {
                if (x > getWidth() - 12) {
                    x = 14;
                    y += 18;
                }
                addDrawableChild(RecipeUnlockWidget.fromId(x, y, unlock));
                x += 18;
            }
            y += 20;
        }

        if (!research.rewards().isEmpty()) {
            addDrawableChild(new StringWidget(12, y, ResearchScreen.getSidebarWidth() - 24, 10, ModUtils.getScreenTranslated("rewards"), client.font));
            y += 10;
            int x = 14;
            for (ResearchReward reward : research.rewards()) {
                if (x > getWidth() - 12) {
                    x = 14;
                    y += 18;
                }
                addDrawableChild(ResearchRewardWidget.create(x, y, reward));
                x += 18;
            }
            y += 20;
        }

        ResearchDescriptionWidget descriptionWidget = new ResearchDescriptionWidget(12, y, getWidth() - 24, research.getDescription(parent.researchManager), client.font);
        if (descriptionWidget.getHeight() > 0)
            addDrawableChild(descriptionWidget);

        if (researchTracker.canResearch(research)) {
            boolean isCurrent = researchTracker.isCurrentOrPinned(research);
            addDrawableChild(StartResearchButton.create(getWidth() - 8, getHeight() - 28, researchTracker, research, !isResearchable, isCurrent));
        }

        offsetY = 0;
        ScreenRectangle rect = rectWithPadding(getContentsRect(), 4);
        if (rect.height() > getHeight()) {
            scrollBounds = MinMaxBounds.Ints.between(-rect.bottom() + getHeight(), -rect.top());
            isScrollable = true;
        }
        else {
            scrollBounds = MinMaxBounds.Ints.exactly(0);
            isScrollable = false;
        }
    }

    @Override
    public ScreenRectangle getContentsRect() {
        ScreenRectangle viewRect = new ScreenRectangle(4, 4, width - 8, height - 8);
        return children().stream()
                .map(GuiEventListener::getRectangle)
                .reduce(viewRect, AbstractResearchView::combineRects);
    }

    @Override
    public void extractView(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (currentDisplay == null || ResearchScreen.selected == null || researchTracker == null) return;

        graphics.fill(0, 0, width, height, BACKGROUND_COLOR);
        graphics.outline(0, 0, width, height, BORDER_COLOR);

        graphics.pose().translate(getOffsetX(), getOffsetY());

        int newMouseX = (int)toOffsetX(mouseX);
        int newMouseY = (int)toOffsetY(mouseY);

        ResearchProgress progress = researchTracker.getProgress(ResearchScreen.selected);
        graphics.text(client.font, researchTracker.getTitleWithStatus(ResearchScreen.selected), 8, 8, CommonColors.WHITE);
        extractCriterion(currentDisplay, ResearchScreen.selected.trigger(), progress, graphics, newMouseX, newMouseY, 12, 20, delta);
        super.extractView(graphics, newMouseX, newMouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        offsetY += verticalAmount * ResearcherConfigs.client.researchTreeScrollSensitivity.get();
        offsetY = Math.clamp(offsetY, scrollBounds.min().orElse(0), scrollBounds.max().orElse(0));
        return true;
    }

    @Override
    public boolean isScrollable() {
        return isScrollable;
    }

    @Override
    public int getOffsetY() {
        return (int)offsetY;
    }

    public static <T extends CriterionTriggerInstance> TriggerDisplayElement prepareDisplay(ResearchCriterion<T> criterion) {
        TriggerHandler<T> handler = TriggerHandlerRegistry.get(criterion.trigger());
        return handler.prepare(criterion);
    }

    public static void extractCriterion(TriggerDisplayElement display, ResearchCriterion<?> criterion, ResearchProgress progress, final GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y, float delta) {
        int criterionWidth = display.width();
        graphics.fill(x, y, x + criterionWidth + 4, y + 18, CRITERION_BACKGROUND);
        graphics.fill(x, y, x + getScaledProgress(criterion, progress, criterionWidth + 4), y + 18, CRITERION_PROGRESS);
        graphics.outline(x, y, criterionWidth + 4, 18, CRITERION_BORDER);
        display.extractRenderState(graphics, x + 1, y + 1, mouseX, mouseY, delta);
    }

    public static int getScaledProgress(ResearchCriterion<?> criterion, ResearchProgress progress, int width) {
        if (progress.isFinished()) return width;
        int count = progress.getCount();
        int max = criterion.count();
        return (int)((float)count / max * width);
    }
}
