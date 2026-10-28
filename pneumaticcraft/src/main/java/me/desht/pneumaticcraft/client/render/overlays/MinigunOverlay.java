package me.desht.pneumaticcraft.client.render.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.minigun.MinigunItem;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import me.desht.pneumaticcraft.client.gui.compat.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.lwjgl.opengl.GL11;

public class MinigunOverlay implements net.neoforged.neoforge.client.gui.GuiLayer {
    private static final float MINIGUN_TEXT_SIZE = 0.55f;

    @Override
    public void render(net.minecraft.client.gui.GuiGraphicsExtractor graphics_raw, DeltaTracker partialTicks) {
        me.desht.pneumaticcraft.client.gui.compat.GuiGraphics graphics = me.desht.pneumaticcraft.client.gui.compat.GuiGraphics.of(graphics_raw);
        Minecraft mc = Minecraft.getInstance();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        Player player = mc.player;

        if (player == null || !(player.getMainHandItem().getItem() instanceof MinigunItem itemMinigun) || !Minecraft.getInstance().options.getCameraType().isFirstPerson())
            return;
        ItemStack heldStack = player.getMainHandItem();
        Minigun minigun = itemMinigun.getMinigun(heldStack, player);

        ItemStack ammo = minigun.getAmmoStack();
        if (!ammo.isEmpty()) {
            graphics.renderItem(ammo, width / 2 + 16, height / 2 - 7);
            int remaining = ammo.getMaxDamage() - ammo.getDamageValue();
            graphics.pose().pushPose();
            graphics.pose().translate(width / 2f + 32, height / 2f - 1, 0);
            graphics.pose().scale(MINIGUN_TEXT_SIZE, MINIGUN_TEXT_SIZE, 1f);
            String text = remaining + "/" + ammo.getMaxDamage();
            graphics.drawString(mc.font, text, 1, 0, 0, false);
            graphics.drawString(mc.font, text, -1, 0, 0, false);
            graphics.drawString(mc.font, text, 0, 1, 0, false);
            graphics.drawString(mc.font, text, 0, -1, 0, false);
            graphics.drawString(mc.font, text, 0, 0, minigun.getAmmoColor(), false);
            graphics.pose().popPose();
        }
        graphics.blit(Textures.MINIGUN_CROSSHAIR, width / 2 - 7, height / 2 - 7, 0, 0, 16, 16, 16, 16);
    }

    @EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
    public static class Listener {
        @SubscribeEvent
        public static void crosshairsEvent(RenderGuiLayerEvent.Pre event) {
            boolean firstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
            if (event.getName().equals(VanillaGuiLayers.CROSSHAIR)
                    && ClientUtils.getClientPlayer().getMainHandItem().getItem() instanceof MinigunItem
                    && firstPerson) {
                event.setCanceled(true);
            }
        }
    }
}
