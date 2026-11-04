/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import li.cil.oc2.client.gui.widget.ImageButton;
import li.cil.oc2.client.gui.widget.ToggleImageButton;
import li.cil.oc2.common.Constants;
import li.cil.oc2.common.container.AbstractMachineTerminalContainer;
import li.cil.oc2.common.util.TooltipUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static li.cil.oc2.common.util.TextFormatUtils.withFormat;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractMachineTerminalScreen<T extends AbstractMachineTerminalContainer> extends AbstractModContainerScreen<T> {
    private static final int CONTROLS_TOP = 8;
    private static final int ENERGY_TOP = CONTROLS_TOP + Sprites.SIDEBAR_3.height + 4;

    private boolean mouseClicked;

    private final MachineTerminalWidget terminalWidget;

    ///////////////////////////////////////////////////////////////////

    protected AbstractMachineTerminalScreen(final T container, final Inventory playerInventory, final Component title) {
        super(container, playerInventory, title);
        // imageWidth/imageHeight are final in 26.1, so the terminal size is applied by
        // positioning leftPos/topPos in init() instead.
        this.terminalWidget = new MachineTerminalWidget(this);
    }

    ///////////////////////////////////////////////////////////////////

    public List<Rect2i> getExtraAreas() {
        final List<Rect2i> list = new ArrayList<>();
        list.add(new Rect2i(
            leftPos - Sprites.SIDEBAR_3.width, topPos + CONTROLS_TOP,
            Sprites.SIDEBAR_3.width, Sprites.SIDEBAR_3.height
        ));

        if (shouldRenderEnergyBar()) {
            list.add(new Rect2i(
                leftPos - Sprites.SIDEBAR_2.width, topPos + ENERGY_TOP,
                Sprites.SIDEBAR_2.width, Sprites.SIDEBAR_2.height
            ));
        }

        return list;
    }

    @Override
    public void containerTick() {
        super.containerTick();

        terminalWidget.tick();
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        return terminalWidget.charTyped((char) event.codepoint(), 0) ||
            super.charTyped(event);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        mouseClicked = true;
        if (!terminalWidget.mouseClicked(event.x(), event.y(), event.button())) {
            return super.mouseClicked(event, doubleClick);
        }
        return true;
    }

    @Override
    public void mouseMoved(double x, double y) {
        terminalWidget.mouseMoved(x, y);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        return terminalWidget.mouseScrolled(scrollY);
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        if (!mouseClicked) return super.mouseReleased(event);
        if (!terminalWidget.mouseReleased(event.x(), event.y(), event.button())) {
            return super.mouseReleased(event);
        }
        return true;
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (terminalWidget.keyPressed(event.key(), event.scancode(), event.modifiers())) {
            return true;
        }

        // Don't close with inventory binding since we usually want to use that as terminal input
        // even without input capture enabled.
        final InputConstants.Key input = InputConstants.getKey(event);
        if (getMinecraft().options.keyInventory.isActiveAndMatches(input)) {
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public void init() {
        super.init();

        // Center the screen using the terminal sprite size, since imageWidth/imageHeight are final.
        leftPos = (width - Sprites.TERMINAL_SCREEN.width) / 2;
        topPos = (height - Sprites.TERMINAL_SCREEN.height) / 2;

        terminalWidget.init();

        final EditBox focusIndicatorEditBox = new EditBox(font, 0, 0, 0, 0, Component.empty());
        focusIndicatorEditBox.setFocused(true);
        setFocusIndicatorEditBox(focusIndicatorEditBox);

        addRenderableWidget(new ToggleImageButton(
            leftPos - Sprites.SIDEBAR_3.width + 4, topPos + CONTROLS_TOP + 4,
            12, 12,
            Sprites.POWER_BUTTON_BASE,
            Sprites.POWER_BUTTON_PRESSED,
            Sprites.POWER_BUTTON_ACTIVE
        ) {
            @Override
            protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
            }

            @Override
            public void onPress(final InputWithModifiers input) {
                super.onPress(input);
                menu.sendPowerStateToServer(!menu.getVirtualMachine().isRunning());
            }

            @Override
            public boolean isToggled() {
                return menu.getVirtualMachine().isRunning();
            }
        }).withTooltip(
            Component.translatable(Constants.COMPUTER_SCREEN_POWER_CAPTION),
            Component.translatable(Constants.COMPUTER_SCREEN_POWER_DESCRIPTION)
        );

        addRenderableWidget(new ToggleImageButton(
            leftPos - Sprites.SIDEBAR_3.width + 4, topPos + CONTROLS_TOP + 4 + 14,
            12, 12,
            Sprites.INPUT_BUTTON_BASE,
            Sprites.INPUT_BUTTON_PRESSED,
            Sprites.INPUT_BUTTON_ACTIVE
        ) {
            @Override
            protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
            }

            @Override
            public void onPress(final InputWithModifiers input) {
                super.onPress(input);

                getMenu().toggleCaptureInputState();
            }

            @Override
            public boolean isToggled() {
                return getMenu().getCaptureInputState();
            }
        }).withTooltip(
            Component.translatable(Constants.TERMINAL_CAPTURE_INPUT_CAPTION),
            Component.translatable(Constants.TERMINAL_CAPTURE_INPUT_DESCRIPTION)
        );

        addRenderableWidget(new ImageButton(
            leftPos - Sprites.SIDEBAR_3.width + 4, topPos + CONTROLS_TOP + 4 + 14 + 14,
            12, 12,
            Sprites.INVENTORY_BUTTON_INACTIVE,
            Sprites.INVENTORY_BUTTON_ACTIVE
        ) {
            @Override
            protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
            }

            @Override
            public void onPress(final InputWithModifiers input) {
                menu.switchToInventory();
            }
        }).withTooltip(Component.translatable(Constants.MACHINE_OPEN_INVENTORY_CAPTION));
    }

    @Override
    public void onClose() {
        super.onClose();
        terminalWidget.onClose();
    }

    ///////////////////////////////////////////////////////////////////

    // We use this text box to indicate to Forge that we want all input, and event handlers should not be allowed
    // to steal input from us (e.g. via custom key bindings). Since Forge is lazy and just uses getDeclaredFields
    // to get private fields, which completely skips fields in base classes, we require subclasses to hold the field...
    protected abstract void setFocusIndicatorEditBox(final EditBox editBox);

    @Override
    protected void renderFg(final GuiGraphicsExtractor graphics, final float partialTicks, final int mouseX, final int mouseY) {
        super.renderFg(graphics, partialTicks, mouseX, mouseY);

        if (shouldRenderEnergyBar()) {
            final int x = leftPos - Sprites.SIDEBAR_2.width + 4;
            final int y = topPos + ENERGY_TOP + 4;
            Sprites.ENERGY_BAR.drawFillY(graphics, x, y, menu.getEnergy() / (float) menu.getEnergyCapacity());
        }

        terminalWidget.render(graphics, menu.getVirtualMachine().getError());
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        Sprites.SIDEBAR_3.draw(graphics, leftPos - Sprites.SIDEBAR_3.width, topPos + CONTROLS_TOP);

        if (shouldRenderEnergyBar()) {
            final int x = leftPos - Sprites.SIDEBAR_2.width;
            final int y = topPos + ENERGY_TOP;
            Sprites.SIDEBAR_2.draw(graphics, x, y);
            Sprites.ENERGY_BASE.draw(graphics, x + 4, y + 4);
        }

        terminalWidget.renderBackground(graphics, mouseX, mouseY);
    }

    @Override
    protected void extractTooltip(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);

        if (shouldRenderEnergyBar()) {

            if (isMouseOver(mouseX, mouseY, -Sprites.SIDEBAR_2.width + 4, ENERGY_TOP + 4, Sprites.ENERGY_BAR.width, Sprites.ENERGY_BAR.height)) {
                final List<? extends FormattedText> tooltip = asList(
                    Component.translatable(Constants.TOOLTIP_ENERGY,
                        withFormat(menu.getEnergy() + "/" + menu.getEnergyCapacity(), ChatFormatting.GREEN)),
                    Component.translatable(Constants.TOOLTIP_ENERGY_CONSUMPTION,
                        withFormat(String.valueOf(menu.getEnergyConsumption()), ChatFormatting.GREEN))
                );
                TooltipUtils.drawTooltip(graphics, tooltip, mouseX, mouseY, 200);
            }
        }
    }

    @Override
    protected void extractLabels(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY) {
        // This is required to prevent the labels from being rendered
    }

    ///////////////////////////////////////////////////////////////////

    private boolean shouldRenderEnergyBar() {
        return menu.getEnergyCapacity() > 0;
    }
}
