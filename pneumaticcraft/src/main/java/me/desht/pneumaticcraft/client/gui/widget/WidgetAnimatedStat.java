/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.StatPanelLayout;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftContainerScreen;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import me.desht.pneumaticcraft.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.joml.Matrix3f;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static net.minecraft.util.LightCoordsUtil.FULL_BRIGHT;

public class WidgetAnimatedStat extends AbstractWidget implements IGuiAnimatedStat {
    private static final int MIN_WIDTH_HEIGHT = 17;
    private static final int MAX_VISIBLE_LINES = 12;
    private static final int SCROLLBAR_MARGIN_WIDTH = 20;
    private static final int TOP_MARGIN_HEIGHT = 20;

    private IGuiAnimatedStat statAbove;
    private StatIcon statIcon;
    private final Screen gui;
    // the text we want to draw
    private final List<Component> textComponents = new ArrayList<>();
    // the text which is actually rendered, after having been wrapped to fit the stat's width
    private final List<FormattedCharSequence> reorderingProcessors = new ArrayList<>();
    // for each rendered line, should it be drawn with a drop shadow?
    private final List<Boolean> dropShadows = new ArrayList<>();
    private final List<AbstractWidget> subWidgets = new ArrayList<>();
    private int effectiveY;  // where the widget is actually rendered (if it has a "parent" stat, it will always render below that)
    private int reservedLines = 0; // space at the top where text isn't rendered
    private boolean autoLineWrap = true;

    // for interpolation purposes, to smoothly animate the widget expanding/contracting
    private int prevX;
    private int prevEffectiveY;
    private int prevWidth;
    private int prevHeight;

    private boolean isClicked = false;  // is the stat currently open?

    private int minWidth = MIN_WIDTH_HEIGHT;
    private int minHeight = MIN_WIDTH_HEIGHT;
    private int minExpandedHeight;
    private int minExpandedWidth;
    private int expandedWidth; // width of the stat when expanded
    private int expandedHeight; // height of the stat when expanded

    private int backGroundColor;
    private TintColor bgColorHi, bgColorLo;
    private boolean leftSided; // determines if the stat expands to the left or right
    private boolean doneExpanding;  // when true, the stat is fully open and text/subwidgets can be rendered
    private int curScroll;  // current scroll position
    private int lineSpacing = 10;
    private int widgetOffsetLeft = 0;
    private int widgetOffsetRight = 0;
    private boolean bevel = false;
    private WidgetVerticalScrollbar scrollBar = null;
    private boolean needTextRecalc = true;
    private int foregroundColor = 0xFFFFFFFF;
    private int titleColor = 0xFFFFFF00;
    private List<Component> extraTooltipText = new ArrayList<>();
    private BooleanSupplier displayPredicate = () -> true;

    public WidgetAnimatedStat(Screen gui, Component title, int xPos, int yPos, int backGroundColor,
                              IGuiAnimatedStat statAbove, boolean leftSided) {
        super(xPos, yPos, MIN_WIDTH_HEIGHT, MIN_WIDTH_HEIGHT, title);

        this.gui = gui;
        this.statAbove = statAbove;
        this.leftSided = leftSided;
        this.statIcon = StatIcon.NONE;
        this.backGroundColor = backGroundColor;
        calculateColorHighlights(this.backGroundColor);

        this.effectiveY = getY();
        if (statAbove != null) {
            this.effectiveY += statAbove.getEffectiveY() + statAbove.getStatHeight();
        }
    }

    public WidgetAnimatedStat(Screen gui, int backgroundColor) {
        this(gui, Component.empty(), 0, 0, backgroundColor, null, false);
    }

    public WidgetAnimatedStat(Screen gui, int backgroundColor, ItemStack icon) {
        this(gui, backgroundColor);
        statIcon = StatIcon.of(icon);
    }

    public WidgetAnimatedStat(Screen gui, int backgroundColor, Identifier texture) {
        this(gui, backgroundColor);
        statIcon = StatIcon.of(texture);
    }

    public WidgetAnimatedStat(Screen gui, Component title, StatIcon icon, int xPos, int yPos, int backGroundColor,
                              IGuiAnimatedStat statAbove, boolean leftSided) {
        this(gui, title, xPos, yPos, backGroundColor, statAbove, leftSided);
        statIcon = icon;
    }

    public WidgetAnimatedStat(Screen gui, Component title, StatIcon icon, int backGroundColor,
                              IGuiAnimatedStat statAbove, StatPanelLayout layout) {
        this(gui, title, 0, 0, backGroundColor, statAbove, layout.expandsLeft());
        Window mw = Minecraft.getInstance().getWindow();
        int x = layout.x() == -1 ? mw.getGuiScaledWidth() - 2 : (int) (mw.getGuiScaledWidth() * layout.x());
        setBaseX(x);
        setBaseY((int) (mw.getGuiScaledHeight() * layout.y()));
        statIcon = icon;
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        needTextRecalc = true;
    }

    @Override
    public void setParentStat(IGuiAnimatedStat stat) {
        statAbove = stat;
    }

    @Override
    public void addSubWidget(AbstractWidget... widget) {
        subWidgets.addAll(Arrays.asList(widget));
    }

    public void removeSubWidget(AbstractWidget widget) {
        subWidgets.remove(widget);
    }

    @Override
    public void setSubwidgetRenderOffsets(int left, int right) {
        widgetOffsetLeft = left;
        widgetOffsetRight = right;
    }

    @Override
    public boolean isLeftSided() {
        return leftSided;
    }

    @Override
    public void setLeftSided(boolean leftSided) {
        this.leftSided = leftSided;
    }

    @Override
    public void setAutoLineWrap(boolean wrap) {
        autoLineWrap = wrap;
    }

    @Override
    public Component getTitle() {
        return getMessage();
    }

    @Override
    public void setTitle(Component title) {
        setMessage(title);
    }

    @Override
    public IGuiAnimatedStat setText(List<Component> text) {
        textComponents.clear();
        textComponents.addAll(text);
        needTextRecalc = true;
        return this;
    }

    @Override
    public IGuiAnimatedStat setText(Component text) {
        textComponents.clear();
        textComponents.add(text);
        needTextRecalc = true;
        return this;
    }

    @Override
    public void appendText(List<Component> text) {
        textComponents.addAll(text);
        needTextRecalc = true;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        if (backgroundColor != this.backGroundColor) {
            this.backGroundColor = backgroundColor;
            calculateColorHighlights(backgroundColor);
        }
    }

    @Override
    public void setForegroundColor(int foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    @Override
    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
    }

    @Override
    public int getBackgroundColor() {
        return backGroundColor;
    }

    private void calculateColorHighlights(int color) {
        if (ConfigHelper.client().general.guiBevel.get()) {
            float fgR = (float) (color >> 16 & 255) / 255.0F;
            float fgG = (float) (color >> 8 & 255) / 255.0F;
            float fgB = (float) (color & 255) / 255.0F;
            float fgA = (float) (color >> 24 & 255) / 255.0F;
            TintColor c = new TintColor(fgR, fgG, fgB, fgA);
            if (bevel) {
                bgColorHi = c.brighter();
                bgColorLo = c.darker();
            } else {
                bgColorHi = c.darker().darker();
                bgColorLo = bgColorHi;
            }
        } else {
            bgColorLo = bgColorHi = new TintColor(color).darker().darker();
        }
    }

    @Override
    public void setBeveled(boolean bevel) {
        this.bevel = bevel;
        calculateColorHighlights(backGroundColor);
    }

    private int getVisibleLines() {
        return MAX_VISIBLE_LINES - reservedLines;
    }

    @Override
    public void setReservedLines(int reservedLines) {
        this.reservedLines = Mth.clamp(reservedLines, 0, MAX_VISIBLE_LINES - 1);
        needTextRecalc = true;
    }

    private void recalcText() {
        reorderingProcessors.clear();
        dropShadows.clear();

        Font font = Minecraft.getInstance().font;
        int titleWidth = font.width(getMessage());
        if (autoLineWrap) {
            int availableWidth = calculateAvailableWidth();
            reorderingProcessors.addAll(GuiUtils.wrapTextComponentList(textComponents, availableWidth, font));
            expandedWidth = Math.min(availableWidth, Math.max(titleWidth, minExpandedWidth));
        } else {
            expandedWidth = titleWidth;
            textComponents.forEach(c -> reorderingProcessors.add(c.getVisualOrderText()));
        }
        reorderingProcessors.forEach(processedLine -> {
            expandedWidth = Math.max(expandedWidth, font.width(processedLine));
            dropShadows.add(needsDropShadow(processedLine));
        });
        expandedWidth += SCROLLBAR_MARGIN_WIDTH;
        int topMargin = reorderingProcessors.isEmpty() ? font.lineHeight : TOP_MARGIN_HEIGHT;
        expandedHeight = Math.max(minExpandedHeight, topMargin + Math.min(MAX_VISIBLE_LINES, reorderingProcessors.size() + reservedLines) * font.lineHeight) + 3;

        addOrRemoveScrollbar();

        needTextRecalc = false;
    }

    /**
     * Calculate the maximum available width that a line of wrapped text can be.  Note that the actual widget might
     * not necessarily end up this wide, depending on the text contents.
     *
     * @return the maximum available width for the animated stat, not including the gutter width for the scrollbar/icon
     */
    private int calculateAvailableWidth() {
        int availableWidth;
        if (gui instanceof AbstractContainerScreen<?> screen) {
            availableWidth = Math.min(Math.max(minExpandedWidth, screen.getXSize()), leftSided ? screen.getGuiLeft() : screen.width - (screen.getGuiLeft() + screen.getXSize()));
        } else {
            availableWidth = leftSided ? getX() : Minecraft.getInstance().getWindow().getGuiScaledWidth() - getX();
        }
        return Math.max(availableWidth - 5 - SCROLLBAR_MARGIN_WIDTH, 16);  // leave at least 5 pixel margin from edge of screen
    }

    private boolean needsDropShadow(FormattedCharSequence line) {
        StyleChecker styleChecker = new StyleChecker(foregroundColor);
        line.accept(styleChecker);
        return styleChecker.isLightColor();
    }

    private void addOrRemoveScrollbar() {
        if (reorderingProcessors.size() > getVisibleLines()) {
            if (subWidgets.contains(scrollBar)) return;
            // need to add a scrollbar
            curScroll = 0;
            int scrollbarHeight = getVisibleLines() * lineSpacing - TOP_MARGIN_HEIGHT;
            int yOffset = reservedLines > 0 ? reservedLines * Minecraft.getInstance().font.lineHeight : 0;
            addSubWidget(scrollBar = new WidgetVerticalScrollbar(leftSided ? -16 : 2, TOP_MARGIN_HEIGHT + yOffset, scrollbarHeight)
                    .setStates(reorderingProcessors.size() - getVisibleLines())
                    .setListening(true));
        } else if (subWidgets.removeIf(w -> w == scrollBar)) {
            // removing existing scrollbar
            curScroll = 0;
            scrollBar = null;
        }
    }

    @Override
    public void setMinimumContractedDimensions(int minWidth, int minHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        width = minWidth;
        height = minHeight;
    }

    @Override
    public void setMinimumExpandedDimensions(int minWidth, int minHeight) {
        if (minExpandedWidth != minWidth) needTextRecalc = true;
        minExpandedWidth = minWidth;
        minExpandedHeight = minHeight;
    }

    @Override
    public void tickWidget() {
        if (needTextRecalc) recalcText();

        prevX = getX();
        prevEffectiveY = effectiveY;
        prevWidth = width;
        prevHeight = height;

        doneExpanding = true;
        // 4 ticks to fully expand/contract
        int expandX = expandedWidth / 4;
        int expandY = expandedHeight / 4;

        if (isClicked) {
            // expand the box
            width = Math.min(expandedWidth, width + expandX);
            height = Math.min(expandedHeight, height + expandY);
            doneExpanding = width == expandedWidth && height == expandedHeight;

            int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            if (isLeftSided()) {
                if (getX() >= scaledWidth) setX(scaledWidth);
            } else {
                if (getX() < 0) setX(1);
            }
            if (getY() + height >= scaledHeight) {
                setY(scaledHeight - height - 1);
            }

            if (doneExpanding && scrollBar != null) curScroll = scrollBar.getState();
        } else {
            // contract the box
            width = Math.max(minWidth, width - expandX);
            height = Math.max(minHeight, height - expandY);
            doneExpanding = false;
        }

        effectiveY = getY();
        if (statAbove != null) {
            effectiveY += statAbove.getEffectiveY() + statAbove.getStatHeight();
        }
    }

    @Override
    public void renderStat(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
        // just delegate to the renderButton() method
        // a separately-named interface method is used to avoid AbstractMethodError problems arising
        // from having a renderButton() method in IGuiAnimatedStat
        extractWidgetRenderState(graphics, x, y, partialTicks);
    }

    @Override
    public void extractWidgetRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics_raw, int mouseX, int mouseY, float partialTicks) {
        GuiGraphics graphics = GuiGraphics.of(graphics_raw);
        if (!this.visible) {
            return;
        }

        int baseX = leftSided ? this.getX() - this.width : this.getX();
        this.isHovered = mouseX >= baseX && mouseY >= this.effectiveY && mouseX < baseX + this.width && mouseY < this.effectiveY + this.height;

        Font fontRenderer = Minecraft.getInstance().font;
        int renderBaseX = (int) Mth.lerp(partialTicks, prevX, getX());
        int renderAffectedY = (int) Mth.lerp(partialTicks, prevEffectiveY, effectiveY);
        int renderWidth = (int) Mth.lerp(partialTicks, prevWidth, width);
        int renderHeight = (int) Mth.lerp(partialTicks, prevHeight, height);
        int xOff = 1;

        if (leftSided) {
            renderWidth *= -1;
            xOff = -1;
        }
        graphics.fill(renderBaseX, renderAffectedY, renderBaseX + renderWidth, renderAffectedY + renderHeight, backGroundColor);
        int sideU = bgColorHi.getARGB();
        int sideD = bgColorLo.getARGB();
        int sideL = leftSided ? sideD : sideU;
        int sideR = leftSided ? sideU : sideD;
        graphics.fill(renderBaseX, renderAffectedY - 1, renderBaseX + renderWidth, renderAffectedY, sideU);
        graphics.fill(renderBaseX + renderWidth, renderAffectedY, renderBaseX + renderWidth + xOff, renderAffectedY + renderHeight, sideR);
        graphics.fill(renderBaseX, renderAffectedY + renderHeight, renderBaseX + renderWidth, renderAffectedY + renderHeight + 1, sideD);
        graphics.fill(renderBaseX - xOff, renderAffectedY, renderBaseX, renderAffectedY + renderHeight, sideL);
        if (leftSided) renderWidth *= -1;

        // if done expanding, draw the information
        if (doneExpanding) {
            if (needTextRecalc) recalcText();

            String title = getMessage().getString();
            int titleYoffset = title.isEmpty() ? 3 : 12;
            if (!title.isEmpty()) {
                graphics.drawString(fontRenderer, title, renderBaseX + (leftSided ? -renderWidth + 2 : 18), renderAffectedY + 2, titleColor);
            }
            for (int i = curScroll; i < reorderingProcessors.size() && i < curScroll + getVisibleLines(); i++) {
                FormattedCharSequence line = reorderingProcessors.get(i);
                int renderX = renderBaseX + (leftSided ? -renderWidth + 2 : 18);
                int renderY = renderAffectedY + (i - curScroll) * lineSpacing + titleYoffset + reservedLines * fontRenderer.lineHeight;
                graphics.drawString(fontRenderer, line,renderX, renderY, foregroundColor, dropShadows.get(i));
            }

            graphics.pose().pushPose();
            graphics.pose().translate(renderBaseX + (leftSided ? widgetOffsetLeft : widgetOffsetRight), renderAffectedY + (titleYoffset - 10), 0);
            subWidgets.forEach(widget -> widget.extractRenderState(graphics.raw(), mouseX - renderBaseX, mouseY - renderAffectedY, partialTicks));
            graphics.pose().popPose();
        }
        if (renderHeight > 16 && renderWidth > 16 && statIcon != null) {
            statIcon.render(graphics, renderBaseX, renderAffectedY, leftSided);
        }

        graphics.renderTooltip(fontRenderer, makeTooltip(mouseX, mouseY), Optional.empty(), mouseX, mouseY);
    }

    private List<Component> makeTooltip(int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        if (mouseIsHoveringOverIcon(mouseX, mouseY)) {
            lines.add(getMessage());
            lines.addAll(getExtraTooltipText());
        }

        return lines;
    }

    private void toggle() {
        isClicked = !isClicked;
        if (isClicked && gui instanceof AbstractPneumaticCraftContainerScreen) {
            // close any other open stat on the same side of the gui
            List<IGuiAnimatedStat> otherStats = ((AbstractPneumaticCraftContainerScreen<?,?>) gui).getStatWidgets();
            otherStats.stream()
                    .filter(stat -> this != stat && stat.isLeftSided() == isLeftSided())
                    .forEach(IGuiAnimatedStat::closeStat);
            // focus on the first textfield child of this widget, if any
            subWidgets.stream()
                    .filter(w -> w instanceof EditBox)
                    .findFirst()
                    .ifPresent(w -> w.setFocused(true));
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible && getBounds().contains((int)mouseX, (int)mouseY);
    }

    /*
     * button: 0 = left 1 = right 2 = middle
     */
    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent _ev, boolean _dbl) {
        double mouseX = _ev.x(), mouseY = _ev.y(); int button = _ev.button();
        if (isHovered()) {
            for (AbstractWidget widget : subWidgets) {
                if (widget.mouseClicked(me.desht.pneumaticcraft.client.gui.compat.GuiCompat.mouse(mouseX - this.getX(), mouseY - this.effectiveY, button), false)) {
                    gui.setFocused(widget);
                    return false;
                } else {
                    widget.setFocused(false);
                }
            }
            // no sub-widgets took the click; toggle this animated stat open/closed
            toggle();
            return true;
        }

        return super.mouseClicked(_ev, _dbl);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent _ev) {
        double mouseX = _ev.x(), mouseY = _ev.y(); int button = _ev.button();
        if (isHovered()) {
            for (AbstractWidget widget : subWidgets) {
                if (widget.mouseReleased(me.desht.pneumaticcraft.client.gui.compat.GuiCompat.mouse(mouseX - this.getX(), mouseY - this.effectiveY, button))) {
                    return true;
                }
            }
            return true;
        }
        return super.mouseReleased(_ev);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent _ev, double dragX, double dragY) {
        double mouseX = _ev.x(), mouseY = _ev.y(); int button = _ev.button();
        if (isHovered()) {
            Rect2i bounds = getBounds();
            for (AbstractWidget widget : subWidgets) {
                if (widget.mouseDragged(me.desht.pneumaticcraft.client.gui.compat.GuiCompat.mouse(mouseX - bounds.getX(), mouseY - bounds.getY(), button), dragX, dragY)) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dirX, double dirY) {
        Rect2i bounds = getBounds();
        for (AbstractWidget widget : subWidgets) {
            if (widget.isFocused() && widget.mouseScrolled(x - bounds.getX(), y - bounds.getY(), dirX, dirY)) {
                return true;
            }
        }
        return scrollBar != null && scrollBar.mouseScrolled(x, y, dirX, dirY);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent _ev) {
        int keyCode = _ev.key(), scanCode = _ev.scancode(), modifiers = _ev.modifiers();
        for (AbstractWidget widget : subWidgets) {
            if (widget.keyPressed(me.desht.pneumaticcraft.client.gui.compat.GuiCompat.key(keyCode, scanCode, modifiers)) || (widget instanceof EditBox && widget.isFocused()) && keyCode != GLFW.GLFW_KEY_ESCAPE) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent _ev) {
        char codePoint = (char) _ev.codepoint(); int modifiers = 0;
        for (AbstractWidget widget : subWidgets) {
            if (widget.charTyped(me.desht.pneumaticcraft.client.gui.compat.GuiCompat.chr(codePoint))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void closeStat() {
        isClicked = false;
    }

    @Override
    public void openStat() {
        isClicked = displayPredicate.getAsBoolean();
    }

    @Override
    public boolean isStatOpen() {
        return isClicked;
    }

    @Override
    public void setOpeningPredicate(BooleanSupplier predicate) {
        displayPredicate = predicate;
    }

    @Override
    public int getEffectiveY() {
        return effectiveY;
    }

    @Override
    public int getBaseX() {
        return getX();
    }

    @Override
    public int getBaseY() {
        return getY();
    }

    @Override
    public int getStatHeight() {
        return getHeight();  // nice bit of mapping there...
    }

    @Override
    public int getStatWidth() {
        return getWidth();
    }

    @Override
    public void setBaseY(int y) {
        this.setY(y);
    }

    @Override
    public void setBaseX(int x) {
        this.setX(x);
    }

    @Override
    public boolean isDoneExpanding() {
        return doneExpanding;
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(getX() - (leftSided ? width : 0), effectiveY, width, height);
    }

    public void setExtraTooltipText(List<Component> extraTooltipText) {
        this.extraTooltipText = extraTooltipText;
    }

    private List<Component> getExtraTooltipText() {
        return extraTooltipText;
    }

    private boolean mouseIsHoveringOverIcon(double x, double y) {
        if (leftSided) {
            return x <= this.getX() && x >= this.getX() - 16 && y >= effectiveY && y <= effectiveY + 16;
        } else {
            return x >= this.getX() && x <= this.getX() + 16 && y >= effectiveY && y <= effectiveY + 16;
        }
    }

    @Override
    public void setLineSpacing(int lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    @Override
    public void setTexture(Identifier texture) {
        this.statIcon = StatIcon.of(texture);
    }

    @Override
    public void setTexture(ItemStack itemStack) {
        this.statIcon = StatIcon.of(itemStack);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }

    public static class StatIcon {
        public static final StatIcon NONE = StatIcon.of(ItemStack.EMPTY);

        private final Either<ItemStack,Identifier> texture;

        private StatIcon(Either<ItemStack,Identifier> texture) {
            this.texture = texture;
        }

        public static StatIcon of(ItemStack stack) {
            return new StatIcon(Either.left(stack));
        }

        public static StatIcon of(ItemLike item) {
            return new StatIcon(Either.left(new ItemStack(item, 1)));
        }

        public static StatIcon of(Identifier texture) {
            return new StatIcon(Either.right(texture));
        }

        void render(GuiGraphics graphics, int x, int y, boolean leftSided) {
            texture.ifLeft(stack -> graphics.renderItem(stack, x - (leftSided ? 16 : 0), y))
                    .ifRight(resLoc -> graphics.blit(resLoc, x - (leftSided ? 16 : 0), y, 0, 0, 16, 16, 16, 16));
        }
    }

    private static class StyleChecker implements FormattedCharSink {
        private static final int THRESHOLD = 129;

        Style style = Style.EMPTY;
        private final int defColor;

        private StyleChecker(int defColor) {
            this.defColor = defColor;
        }

        @Override
        public boolean accept(int p_accept_1_, Style p_accept_2_, int p_accept_3_) {
            if (style == Style.EMPTY) style = p_accept_2_;
            return true;
        }

        public boolean isLightColor() {
            int c = style == null || style.isEmpty() || style.getColor() == null ? defColor : style.getColor().getValue();
            return isLightColor(new TintColor(c));
        }

        private boolean isLightColor(TintColor bg) {
            // calculate a foreground color which suitably contrasts with the given background color
            int luminance = (int) Math.sqrt(
                    bg.getRed() * bg.getRed() * 0.241 +
                            bg.getGreen() * bg.getGreen() * 0.691 +
                            bg.getBlue() * bg.getBlue() * 0.068
            );
            return luminance > THRESHOLD;
        }
    }
}
