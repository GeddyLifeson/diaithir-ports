package me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.EnderVisorHandler;
import net.minecraft.client.DeltaTracker;
import me.desht.pneumaticcraft.client.gui.compat.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL11;

public class EnderVisorClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<EnderVisorHandler> {
    public EnderVisorClientHandler() {
        super(CommonUpgradeHandlers.enderVisorHandler);
    }

    public static class PumpkinLayer implements net.neoforged.neoforge.client.gui.GuiLayer {
        private static final Identifier PUMPKIN_OVERLAY = Identifier.parse("textures/misc/pumpkinblur.png");

        @Override
        public void render(net.minecraft.client.gui.GuiGraphicsExtractor graphics_raw, DeltaTracker pDeltaTracker) {
        me.desht.pneumaticcraft.client.gui.compat.GuiGraphics graphics = me.desht.pneumaticcraft.client.gui.compat.GuiGraphics.of(graphics_raw);
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            if (!handler.upgradeUsable(CommonUpgradeHandlers.enderVisorHandler, true)) {
               return;
            }
            graphics.blit(PUMPKIN_OVERLAY, 0, 0, 0, 0, graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight());
        }
    }
}
