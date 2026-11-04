/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.gui;

import li.cil.oc2.client.gui.widget.ImageButton;
import li.cil.oc2.common.Constants;
import li.cil.oc2.common.blockentity.BusCableBlockEntity;
import li.cil.oc2.common.item.Items;
import li.cil.oc2.common.network.Network;
import li.cil.oc2.common.network.message.BusInterfaceNameMessage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import static li.cil.oc2.common.util.TranslationUtils.text;

public final class BusInterfaceScreen extends Screen {
    private static final int TEXT_LEFT = 9;
    private static final int TEXT_TOP = 11;
    private static final int CONFIRM_LEFT = 206;
    private static final int CONFIRM_TOP = 9;
    private static final int CANCEL_LEFT = 219;
    private static final int CANCEL_TOP = 9;

    private final BusCableBlockEntity busCable;
    private final Direction side;

    private EditBox nameField;

    private int left, top;

    ///////////////////////////////////////////////////////////////////

    public BusInterfaceScreen(final BusCableBlockEntity busCable, final Direction side) {
        super(new ItemStack(Items.BUS_INTERFACE.get()).getHoverName());
        this.busCable = busCable;
        this.side = side;
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected void init() {
        super.init();

        //getMinecraft().keyboardHandler.setSendRepeatsToGui(true);

        left = (width - Sprites.BUS_INTERFACE_SCREEN.width) / 2;
        top = (height - Sprites.BUS_INTERFACE_SCREEN.height) / 2;

        nameField = new EditBox(font, left + TEXT_LEFT, top + TEXT_TOP, 192, 12, text("{mod}.gui.bus_interface_name"));
        nameField.setCanLoseFocus(false);
        nameField.setTextColor(0xFFFFFFFF);
        nameField.setBordered(false);
        nameField.setMaxLength(32);
        nameField.setValue(busCable.getInterfaceName(side));
        addWidget(nameField);
        setInitialFocus(nameField);

        addRenderableWidget(new ImageButton(
            left + CONFIRM_LEFT, top + CONFIRM_TOP,
            Sprites.CONFIRM_BASE.width, Sprites.CONFIRM_BASE.height,
            Sprites.CONFIRM_BASE,
            Sprites.CONFIRM_PRESSED
        ) {
            @Override
            protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
            }

            @Override
            public void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTicks) {
                if (isHoveredOrFocused()) {
                    Sprites.CONFIRM_PRESSED.draw(graphics, getX(), getY());
                } else {
                    Sprites.CONFIRM_BASE.draw(graphics, getX(), getY());
                }
            }

            @Override
            public void onPress(final InputWithModifiers input) {
                super.onPress(input);
                setInterfaceName(nameField.getValue());
                onClose();
            }
        }).withTooltip(Component.translatable(Constants.TOOLTIP_CONFIRM));

        addRenderableWidget(new ImageButton(
            left + CANCEL_LEFT, top + CANCEL_TOP,
            Sprites.CANCEL_BASE.width, Sprites.CANCEL_BASE.height,
            Sprites.CANCEL_BASE,
            Sprites.CANCEL_PRESSED
        ) {
            @Override
            protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
            }

            @Override
            public void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTicks) {
                if (isHoveredOrFocused()) {
                    Sprites.CANCEL_PRESSED.draw(graphics, getX(), getY());
                } else {
                    Sprites.CANCEL_BASE.draw(graphics, getX(), getY());
                }
            }

            @Override
            public void onPress(final InputWithModifiers input) {
                super.onPress(input);
                onClose();
            }
        }).withTooltip(Component.translatable(Constants.TOOLTIP_CANCEL));
    }

    @Override
    public void onClose() {
        super.onClose();

        //getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void tick() {
        super.tick();

        final Vec3 busCableCenter = Vec3.atCenterOf(busCable.getBlockPos());
        if (!busCable.isValid() ||
            getMinecraft().player == null ||
            getMinecraft().player.distanceToSqr(busCableCenter) > 8 * 8) {
            onClose();
        }
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        final int keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_ENTER ||
            keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            setInterfaceName(nameField.getValue());
            onClose();
            return true;
        }

        return nameField.keyPressed(event) || super.keyPressed(event);
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTicks) {
        // Background is extracted by the screen itself before this method is called.
        Sprites.BUS_INTERFACE_SCREEN.draw(graphics, left, top);

        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        nameField.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////

    private void setInterfaceName(final String name) {
        Network.sendToServer(BusInterfaceNameMessage.ToServer(busCable, side, name));
    }
}
