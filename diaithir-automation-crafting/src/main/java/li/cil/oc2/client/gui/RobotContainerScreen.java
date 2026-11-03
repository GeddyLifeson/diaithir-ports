/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.gui;

import li.cil.oc2.common.container.RobotInventoryContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class RobotContainerScreen extends AbstractMachineInventoryScreen<RobotInventoryContainer> {
    private static final int SLOT_SIZE = 18;

    ///////////////////////////////////////////////////////////////////

    public static void renderSelection(final GuiGraphicsExtractor graphics, final int selectedSlot, final int x, final int y, final int columns) {
        // TODO(oc2-restore): shader and shader color setup is no longer needed (and no longer possible) in 26.1.

        final int slotX = (selectedSlot % columns) * SLOT_SIZE;
        final int slotY = (selectedSlot / columns) * SLOT_SIZE;
        final int offset = SLOT_SIZE * (int) (15 * (System.currentTimeMillis() % 1000) / 1000);
        Sprites.SLOT_SELECTION.draw(graphics, x + slotX, y + slotY, 0, offset);
    }

    ///////////////////////////////////////////////////////////////////

    public RobotContainerScreen(final RobotInventoryContainer container, final Inventory playerInventory, final Component title) {
        super(container, playerInventory, title);
        // imageWidth and imageHeight are final in 26.1, so the layout is adjusted in init() instead.
        inventoryLabelY = Sprites.ROBOT_CONTAINER.height - 94;
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected void init() {
        super.init();

        // TODO(oc2-restore): imageWidth/imageHeight are final in 26.1; pass the size via the super constructor if the base screen supports it.
        leftPos = (width - Sprites.ROBOT_CONTAINER.width) / 2;
        topPos = (height - Sprites.ROBOT_CONTAINER.height) / 2;
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        Sprites.ROBOT_CONTAINER.draw(graphics, leftPos, topPos);
        renderSelection(graphics, menu.getRobot().getSelectedSlot(), leftPos + 115, topPos + 23, 3);
    }
}
