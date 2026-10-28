package me.desht.pneumaticcraft.client.render.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.JackHammerItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import me.desht.pneumaticcraft.client.gui.compat.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.opengl.GL11;

public class JackhammerOverlay implements net.neoforged.neoforge.client.gui.GuiLayer {
    @Override
    public void render(net.minecraft.client.gui.GuiGraphicsExtractor graphics_raw, DeltaTracker partialTicks) {
        me.desht.pneumaticcraft.client.gui.compat.GuiGraphics graphics = me.desht.pneumaticcraft.client.gui.compat.GuiGraphics.of(graphics_raw);
        Player player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof JackHammerItem)
                || !Minecraft.getInstance().options.getCameraType().isFirstPerson())
            return;
        long timeDelta = player.level().getGameTime() - JackHammerItem.getLastModeSwitchTime();
        JackHammerItem.DigMode digMode = JackHammerItem.getDigMode(player.getMainHandItem());
        boolean showHud = ConfigHelper.client().general.jackHammerHud.get();
        if (digMode.atLeast(JackHammerItem.DigMode.MODE_1X2) && showHud || timeDelta < 30 || player.isShiftKeyDown()) {
            float scaleFactor = Mth.clamp((float) Minecraft.getInstance().getWindow().getGuiScale(), 2, 3);
            graphics.pose().pushPose();
            graphics.pose().translate(graphics.guiWidth() / 2.0, graphics.guiHeight() / 2.0, 0);
            graphics.pose().scale(scaleFactor, scaleFactor, scaleFactor);
            graphics.pose().translate(8, -8, 0);
            graphics.blit(digMode.getGuiIcon(), 0, 0, 0, 0, 16, 16, 16, 16);
            graphics.pose().popPose();
        }
    }
}
