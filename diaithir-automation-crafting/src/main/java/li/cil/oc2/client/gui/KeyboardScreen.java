/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.gui;

import li.cil.oc2.common.blockentity.KeyboardBlockEntity;
import li.cil.oc2.common.item.Items;
import li.cil.oc2.common.network.Network;
import li.cil.oc2.common.network.message.KeyboardInputMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public final class KeyboardScreen extends Screen {
    private static final int BORDER_SIZE = 4;
    private static final float ARM_SWING_RATE = 0.8f;
    private static final int BORDER_COLOR = 0xFFFFFFFF;

    private static final MutableComponent CLOSE_INFO = Component.translatable("gui.oc2r.keyboard.close_info");

    ///////////////////////////////////////////////////////////////////

    private final KeyboardBlockEntity keyboard;

    ///////////////////////////////////////////////////////////////////

    public static boolean hideHotbar = false;

    public KeyboardScreen(final KeyboardBlockEntity keyboard) {
        super(Component.translatable(Items.KEYBOARD.get().getDescriptionId()));
        this.keyboard = keyboard;
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected void init() {
        super.init();

        // Disable hotbar since we don't need it here, and it just blocks screen space.
        hideHotbar = true;
    }

    @Override
    public void tick() {
        super.tick();

        final Vec3 keyboardCenter = Vec3.atCenterOf(keyboard.getBlockPos());
        if (!keyboard.isValid() ||
            getMinecraft().player == null ||
            getMinecraft().player.distanceToSqr(keyboardCenter) > 8 * 8) {
            onClose();
        }
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        sendInputMessage(event.key(), true);
        return true;
    }

    @Override
    public boolean keyReleased(final KeyEvent event) {
        sendInputMessage(event.key(), false);
        return true;
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_2) {
            onClose();
            return true;
        } else {
            return super.mouseClicked(event, doubleClick);
        }
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
        // Don't
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        renderBorderOverlay(graphics);

        graphics.textWithWordWrap(
            font,
            CLOSE_INFO,
            BORDER_SIZE * 3,
            height - BORDER_SIZE * 3 - font.lineHeight,
            width - BORDER_SIZE * 6,
            0x88FFFFFF
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();

        hideHotbar = false;
    }

    ///////////////////////////////////////////////////////////////////

    private void renderBorderOverlay(final GuiGraphicsExtractor graphics) {
        graphics.fill(BORDER_SIZE, BORDER_SIZE, width - BORDER_SIZE, BORDER_SIZE * 2, BORDER_COLOR);
        graphics.fill(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE * 2, height - BORDER_SIZE, BORDER_COLOR);
        graphics.fill(BORDER_SIZE, height - BORDER_SIZE * 2, width - BORDER_SIZE, height - BORDER_SIZE, BORDER_COLOR);
        graphics.fill(width - BORDER_SIZE * 2, BORDER_SIZE, width - BORDER_SIZE, height - BORDER_SIZE, BORDER_COLOR);
    }

    private void sendInputMessage(final int keycode, final boolean isDown) {
        if (KeyCodeMapping.MAPPING.containsKey(keycode)) {
            swingArm();
            final int evdevCode = KeyCodeMapping.MAPPING.get(keycode);
            Network.sendToServer(new KeyboardInputMessage(keyboard, evdevCode, isDown));
        }
    }

    private void swingArm() {
        final Minecraft minecraft = getMinecraft();
        final LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        final RandomSource random = player.getRandom();
        if (random.nextFloat() < ARM_SWING_RATE) {
            return;
        }

        final InteractionHand handToSwing;
        if (minecraft.options.getCameraType().isFirstPerson()) {
            handToSwing = InteractionHand.MAIN_HAND;
        } else {
            handToSwing = random.nextBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        }

        player.swing(handToSwing);
    }
}
