/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import li.cil.oc2.common.item.Items;
import li.cil.oc2.common.item.NetworkInterfaceCardItem;
import li.cil.oc2.common.network.Network;
import li.cil.oc2.common.network.message.NetworkInterfaceCardConfigurationMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import static li.cil.oc2.common.util.TranslationUtils.key;
import static li.cil.oc2.common.util.TranslationUtils.text;

public final class NetworkInterfaceCardScreen extends Screen {
    private static final String SIDE_STATE_TEXT = key("gui.{mod}.network_interface_card.side_state");
    private static final Component CONNECTIVITY_ENABLED_TEXT = text("gui.{mod}.network_interface_card.connectivity.enabled");
    private static final Component CONNECTIVITY_DISABLED_TEXT = text("gui.{mod}.network_interface_card.connectivity.disabled");
    private static final Component INFO_TEXT = text("gui.{mod}.network_interface_card.info");

    public static final int UI_WIDTH = Sprites.NETWORK_INTERFACE_CARD_SCREEN.width;
    public static final int UI_HEIGHT = Sprites.NETWORK_INTERFACE_CARD_SCREEN.height;
    public static final int BLOCK_LEFT = UI_WIDTH / 2;
    public static final int BLOCK_TOP = 53;
    public static final int INFO_TEXT_LEFT = 8;
    public static final int INFO_TEXT_TOP = 104;
    public static final int INFO_TEXT_WIDTH = UI_WIDTH - 16;
    public static final int MAX_BLOCK_PITCH = 30;

    ///////////////////////////////////////////////////////////////////

    private final Player player;
    private final InteractionHand hand;

    private final ComputerBlockItemRenderer computerBlockItemRenderer = new ComputerBlockItemRenderer();

    private Vector3f blockRotation = new Vector3f(0, 0, 0);
    private int left, top;
    @Nullable private Direction focusedSide;
    private boolean isDraggingBlock, hasDraggedBlock;
    private double dragStartX, dragStartY;

    ///////////////////////////////////////////////////////////////////

    public NetworkInterfaceCardScreen(final Player player, final InteractionHand hand) {
        super(Component.translatable(Items.NETWORK_INTERFACE_CARD.get().getDescriptionId()));
        this.player = player;
        this.hand = hand;
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected void init() {
        super.init();

        left = (width - UI_WIDTH) / 2;
        top = (height - UI_HEIGHT) / 2;
    }

    @Override
    public void tick() {
        super.tick();

        final ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.is(Items.NETWORK_INTERFACE_CARD.get())) {
            onClose();
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        final boolean result = super.mouseClicked(event, doubleClick);

        if (!result && isMouseInBlockArea(event.x(), event.y()) && event.button() == 0) {
            isDraggingBlock = true;
            hasDraggedBlock = false;
            dragStartX = event.x();
            dragStartY = event.y();
        }

        return result;
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        if (isDraggingBlock && event.button() == 0) {
            isDraggingBlock = false;
            if (!hasDraggedBlock && focusedSide != null) {
                final NetworkInterfaceCardConfigurationMessage message = new NetworkInterfaceCardConfigurationMessage(hand, focusedSide, !getConfiguration(focusedSide));
                Network.sendToServer(message);
                Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            }
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(final MouseButtonEvent event, final double deltaX, final double deltaY) {
        if (isDraggingBlock && event.button() == 0) {
            if (!hasDraggedBlock) {
                final double dx = event.x() - dragStartX;
                final double dy = event.y() - dragStartY;
                final double delta = Math.sqrt(dx * dx + dy * dy);
                hasDraggedBlock = delta > 3;
            }
            if (hasDraggedBlock) {
                blockRotation = new Vector3f(
                    Mth.clamp(blockRotation.x() - (float) deltaY * 0.2f, -MAX_BLOCK_PITCH, MAX_BLOCK_PITCH),
                    Mth.wrapDegrees(blockRotation.y() + (float) deltaX * 0.2f),
                    blockRotation.z()
                );
            }
        }

        return true;
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        Sprites.NETWORK_INTERFACE_CARD_SCREEN.draw(graphics, left, top);

        final int blockX = left + BLOCK_LEFT;
        final int blockY = top + BLOCK_TOP;
        focusedSide = computerBlockItemRenderer.getFocusedSide(blockX - mouseX, blockY - mouseY, blockRotation);
        computerBlockItemRenderer.render(graphics, blockX, blockY, blockRotation);

        if (focusedSide != null) {
            final Component enabledComponent = getConfiguration(focusedSide) ? CONNECTIVITY_ENABLED_TEXT : CONNECTIVITY_DISABLED_TEXT;
            final MutableComponent tooltip = Component.translatable(SIDE_STATE_TEXT, enabledComponent);
            //renderTooltip(stack, tooltip, mouseX, mouseY);
        }

        int lineY = top + INFO_TEXT_TOP;
        for (final FormattedCharSequence line : font.split(INFO_TEXT, INFO_TEXT_WIDTH)) {
            graphics.text(font, line, left + INFO_TEXT_LEFT, lineY, 0xFFAAAAAA);
            lineY += font.lineHeight;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////

    private boolean isMouseInBlockArea(final double mouseX, final double mouseY) {
        return mouseX >= left + 37 && mouseX <= left + (37 + 102) &&
            mouseY >= top + 10 && mouseY <= top + (10 + 102);
    }

    private boolean getConfiguration(@Nullable final Direction side) {
        return side != null && NetworkInterfaceCardItem.getSideConfiguration(player.getItemInHand(hand), side);
    }

    ///////////////////////////////////////////////////////////////////

    private final class ComputerBlockItemRenderer {
        public static final int BLOCK_RENDER_SIZE = 48;

        private final ItemStack computerItemStack = new ItemStack(Items.COMPUTER.get());

        @Nullable
        private Direction getFocusedSide(final float mouseX, final float mouseY, final Vector3f rotation) {
            // Rotate ray inversely around block to represent visual block rotation.
            final Quaternionf quaternion = new Quaternionf().rotateXYZ(rotation.x, rotation.y, rotation.z);
            quaternion.conjugate();

            // Move ray in screen space to mouse position.
            final float relMouseX = -mouseX / (float) BLOCK_RENDER_SIZE;
            final float relMouseY = -mouseY / (float) BLOCK_RENDER_SIZE;

            final Vector3f source = new Vector3f();
            source.add(relMouseX, relMouseY, 1);
            source.rotate(quaternion);

            final Vector3f target = new Vector3f();
            target.add(relMouseX, relMouseY, -1);
            target.rotate(quaternion);

            // Intersect rotated ray with bounding box representing block.
            final AABB aabb = new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
            return aabb.clip(new Vec3(source), new Vec3(target))
                .map(hit -> Direction.getApproximateNearest(hit.x(), -hit.y(), hit.z()))
                .filter(side -> side != Direction.SOUTH)
                .orElse(null);
        }

        // TODO(oc2-restore): The rotating 3D block with per-side connectivity overlays relied on the removed
        //  RenderSystem model view stack, ItemRenderer and ModRenderType pipeline. Reduced to a flat item icon.
        public void render(final GuiGraphicsExtractor graphics, final int x, final int y, final Vector3f rotation) {
            final float scale = BLOCK_RENDER_SIZE / 16f;
            graphics.pose().pushMatrix();
            graphics.pose().translate(x - BLOCK_RENDER_SIZE / 2f, y - BLOCK_RENDER_SIZE / 2f);
            graphics.pose().scale(scale, scale);
            graphics.item(computerItemStack, 0, 0);
            graphics.pose().popMatrix();
        }
    }
}
